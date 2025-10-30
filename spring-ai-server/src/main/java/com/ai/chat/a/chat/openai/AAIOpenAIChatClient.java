package com.ai.chat.a.chat.openai;
import cn.hutool.core.util.IdUtil;
import com.ai.chat.a.constant.Constants;
import com.ai.chat.a.dto.UserChatDTO;
import com.ai.chat.a.entity.OpenAIResponse;
import com.ai.chat.a.entity.UserIdea;
import com.ai.chat.a.entity.UserUploadFile;
import com.ai.chat.a.image.qianfan.AAIQianfanImageClient;
import com.ai.chat.a.redis.RedisUtil;
import com.ai.chat.a.rag.ContextMergeService;
import com.ai.chat.a.rag.EmbeddingCacheService;
import com.ai.chat.a.rag.MemoryUpdateService;
import com.ai.chat.a.rag.ParallelRetrievalService;
import com.ai.chat.a.structuredOutput.JSONStructuredOutput;
import com.ai.chat.a.utils.*;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ExecutionException;
import org.springframework.util.DigestUtils;
import reactor.core.publisher.Mono;

@Data
@Slf4j
public class AAIOpenAIChatClient {
       @Autowired
       private OpenAiChatModel chatClient;
       @Autowired
       private AAIQianfanImageClient qianfanImageClient;
       @Autowired
       private AliOssUpload aliOssUpload;
       private String defaultMessage = Constants.DEFAULT_USER_MESSAGE;
       private String defaultModel;
       @Autowired
       private RedisUtil redisUtil;
       @Autowired
       private JSONStructuredOutput jsonStructuredOutput;
       @Autowired
       private ContextMergeService contextMergeService;
       @Autowired
       private EmbeddingCacheService embeddingCacheService;
       @Autowired
       private MemoryUpdateService memoryUpdateService;
       @Autowired
       private ParallelRetrievalService parallelRetrievalService;
       @Autowired
       @Qualifier("aiServiceExecutor")
       private ExecutorService aiServiceExecutor;

       public OpenAIResponse generateRAG(UserChatDTO userChatDTO, String model, List<String> contents) throws IOException {
           try {
               // 提交到线程池异步处理
               Future<OpenAIResponse> future = aiServiceExecutor.submit(() -> {
                   try {
                       return doGenerateRAG(userChatDTO, model, contents);
                   } catch (IOException e) {
                       log.error("RAG生成失败", e);
                       throw new RuntimeException(e);
                   }
               });
               return future.get();
           } catch (InterruptedException | ExecutionException e) {
               log.error("获取RAG生成结果失败", e);
               if (e.getCause() instanceof IOException) {
                   throw (IOException) e.getCause();
               }
               throw new RuntimeException(e);
           }
       }

       private OpenAIResponse doGenerateRAG(UserChatDTO userChatDTO, String model, List<String> contents) throws IOException {
           // 获取用户ID和会话ID
           String userId = ThreadLocalUtil.get();
           String sessionId = userChatDTO.getSessionId();
           String query = userChatDTO.getQuestion();
           
           log.info("开始RAG流程，用户ID: {}, 会话ID: {}, 查询: {}", userId, sessionId, query);
           
           // 0. 检查RAG缓存
           try {
               String cachedAnswer = memoryUpdateService.checkRAGCache(userId, sessionId, query);
               if (cachedAnswer != null && !cachedAnswer.isEmpty()) {
                   log.info("命中RAG缓存，直接返回缓存结果，用户ID: {}, 会话ID: {}", userId, sessionId);
                   return OpenAIResponse.builder()
                           .response(cachedAnswer)
                           .build();
               }
           } catch (Exception e) {
               log.warn("检查RAG缓存失败，继续执行RAG流程，用户ID: {}, 会话ID: {}", userId, sessionId, e);
           }
           
           // 1. 并行检索上下文信息
           try {
               List<String> retrievalResults = parallelRetrievalService.retrieve(userId, sessionId, query, 5).get();
               // 2. 合并上下文
               String context = contextMergeService.mergeContext(retrievalResults, query);
               // 3. 构建提示词
               List<Message> messages = new ArrayList<>();
               // 添加系统提示词
               messages.add(new SystemMessage(Constants.SYSTEM_MESSAGE_PROMPT));
               // 添加上下文信息
               if (!context.isEmpty()) {
                   messages.add(new SystemMessage("以下是与用户问题相关的上下文信息：\n\n" + context + 
                           "\n\n请基于以上上下文信息回答用户的问题，如果上下文信息不足以回答问题，请告知用户。"));
               }
               
               // 4. 处理用户上传文件（如果有）
               List<UserUploadFile> userUploadFile = redisUtil.getUserUploadFile(userChatDTO);
               Boolean updated = redisUtil.updateExpirationTime(userId + sessionId);
               
               if (!updated) {
                   redisUtil.setUserUploadFile(userChatDTO, userUploadFile);
               }
               
               // 5. 构建用户消息
               UserMessage userMessage;
               List<Media> mediaList = new ArrayList<>();
               
               // 检查是否为多模态模型
               UserIdea userIdea = null;
               if (!Constants.N_MODEL.contains(model)) {
                   userIdea = getUserIdea(query);
                   log.info("多模态模型");
               }
               
               // 如果需要图片生成，使用并发处理
               if (userIdea != null && userIdea.getGenerateImage()) {
                   return handleMultiModalTaskWithConcurrency(userChatDTO, model, contents, userUploadFile, userIdea);
               }
               
               // 处理上传的文件
               if (userUploadFile != null && !userUploadFile.isEmpty()) {
                   log.info("上传了文件");
                   
                   for (UserUploadFile uploadFile : userUploadFile) {
                       if (uploadFile.getFileType() == 1) {
                           stringPathToMedia(uploadFile.getSrc(), mediaList);
                       }
                   }
                   
                   userMessage = new UserMessage(query, mediaList);
               } else {
                   log.info("没有上传文件");
                   userMessage = new UserMessage(query);
               }
               
               // 添加用户消息
               messages.add(userMessage);
               
               // 6. 调用大模型
               Prompt prompt = new Prompt(messages, OpenAiChatOptions.builder()
                       .withModel(setDefault(model))
                       .withHttpHeaders(Map.of("Accept-Encoding", "identity"))
                       .build());
               
               log.info("RAG prompt: {}", prompt.toString());
               ChatResponse response = chatClient.call(prompt);
               
               // 7. 获取响应内容
               String answer = response.getResult().getOutput().getContent();
               
               // 8. 异步更新短期记忆和RAG缓存
               memoryUpdateService.updateMemoryAndCacheAsync(userId, sessionId, query, answer);
               
               return OpenAIResponse.builder()
                       .response(answer)
                       .build();
           } catch (Exception e) {
               log.error("RAG检索失败", e);
               throw new IOException("RAG检索失败", e);
           }
       }
       


       /**
        * 使用并发方式处理多模态任务（文本生成 + 图像生成）
        */
       private OpenAIResponse handleMultiModalTaskWithConcurrency(UserChatDTO userChatDTO, String model, List<String> contents,
                                                                 List<UserUploadFile> userUploadFile, UserIdea userIdea) throws IOException {
           CountDownLatch latch = new CountDownLatch(2); // 两个任务：文本生成和图像生成
           AtomicReference<ChatResponse> textResponseRef = new AtomicReference<>();
           AtomicReference<String[]> imageUploadRef = new AtomicReference<>(new String[1]);
           AtomicReference<String> ba64FileRef = new AtomicReference<>("");
           AtomicReference<String> generateFileNameRef = new AtomicReference<>();
           AtomicReference<String> fileSizeRef = new AtomicReference<>();
           AtomicReference<Integer> fileTypeRef = new AtomicReference<>();
           AtomicReference<List<Media>> mediaListRef = new AtomicReference<>(new ArrayList<>());
           
           // 任务1：图像生成
           aiServiceExecutor.submit(() -> {
               try {
                   log.info("开始并发处理图像生成任务");
                   String ba64File;
                   if (userUploadFile != null && !userUploadFile.isEmpty()) {
                       List<String> readMediaFileList = new ArrayList<>();
                       for (UserUploadFile uploadFile : userUploadFile) {
                           if (uploadFile.getFileType() == 1) {
                               readMediaFileList.add(uploadFile.getReadMediaFile().getPrompt() + "," + uploadFile.getReadMediaFile().getDesc());
                           }
                       }
                       String prompts = String.join(",", readMediaFileList);
                       ba64File = qianfanImageClient.getImageFromQianfanSDXL(
                               prompts + "," + userIdea.getPrompt()) + "," + userIdea.getStyle();
                   } else {
                       ba64File = qianfanImageClient.getImageFromQianfanSDXL(userIdea.getPrompt() + "," + userIdea.getStyle());
                   }
                   
                   if (ba64File.isEmpty()) {
                       return;
                   }
                   
                   ba64FileRef.set(ba64File);
                   String generateFileName = IdUtil.simpleUUID();
                   generateFileNameRef.set(generateFileName);
                   
                   MultipartFile multipartFile = Base64ToMultipartFileConverter.base64ToMultipartFile(ba64File, generateFileName);
                   String[] upload = aliOssUpload.upload(multipartFile);
                   imageUploadRef.set(upload);
                   
                   fileSizeRef.set(FileUtil.formatFileSize(multipartFile.getSize()));
                   fileTypeRef.set(FileUtil.getFileType(multipartFile));
                   
                   // 创建media对象
                   List<Media> mediaList = new ArrayList<>();
                   stringPathToMedia(upload[0], mediaList);
                   mediaListRef.set(mediaList);
                   
                   log.info("图像生成任务完成");
               } catch (Exception e) {
                   log.error("图像生成任务失败", e);
               } finally {
                   latch.countDown();
               }
           });
           
           // 任务2：准备文本处理
           aiServiceExecutor.submit(() -> {
               try {
                   log.info("开始并发处理文本任务");
                   List<Message> messages = new ArrayList<>();
                   contents.forEach(content -> {
                       messages.add(new SystemMessage(content));
                   });
                   messages.add(new SystemMessage(Constants.SYSTEM_MESSAGE_PROMPT));
                   
                   // 等待图像任务完成，以获取生成的图像
                   latch.countDown(); // 释放当前任务的计数
                   latch.await(); // 等待另一个任务完成
                   
                   // 图像任务已完成，获取结果
                   List<Media> mediaList = mediaListRef.get();
                   String[] upload = imageUploadRef.get();
                   
                   UserMessage userMessage;
                   if (mediaList != null && !mediaList.isEmpty()) {
                       if (userUploadFile != null && !userUploadFile.isEmpty()) {
                           userMessage = new UserMessage(Constants.FILE_TO_FILE, mediaList);
                       } else {
                           userMessage = new UserMessage(Constants.GET_FILE_CONTENT_PROMPT + " " + userChatDTO.getQuestion(), mediaList);
                       }
                   } else {
                       userMessage = new UserMessage(userChatDTO.getQuestion());
                   }
                   
                   messages.add(userMessage);
                   Prompt prompt = new Prompt(messages, OpenAiChatOptions
                           .builder()
                           .withModel(setDefault(model))
                           .withHttpHeaders(Map.of("Accept-Encoding", "identity"))
                           .build());
                   
                   ChatResponse response = chatClient.call(prompt);
                   textResponseRef.set(response);
                   log.info("文本任务处理完成");
               } catch (Exception e) {
                   log.error("文本任务处理失败", e);
               }
           });
           
           // 等待所有任务完成
           try {
               latch.await();
           } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               log.error("等待任务完成时被中断", e);
           }
           
           ChatResponse response = textResponseRef.get();
           if (response == null) {
               throw new IOException("无法获取AI响应");
           }
           
           return OpenAIResponse.builder()
                   .response(response.getResult().getOutput().getContent())
                   .fileType(fileTypeRef.get())
                   .fileName(generateFileNameRef.get())
                   .fileSize(fileSizeRef.get())
                   .filePath(imageUploadRef.get()[0])
                   .build();
       }



       public AAIOpenAIChatClient(String defaultModel){
               this.defaultModel = defaultModel;
       }
       public OpenAIResponse generate(UserChatDTO userChatDTO, String model) throws IOException {
           try {
               // 提交到线程池异步处理
               Future<OpenAIResponse> future = aiServiceExecutor.submit(() -> {
                   try {
                       return doGenerate(userChatDTO, model);
                   } catch (IOException e) {
                       log.error("AI生成失败", e);
                       throw new RuntimeException(e);
                   }
               });
               return future.get();
           } catch (InterruptedException | ExecutionException e) {
               log.error("获取AI生成结果失败", e);
               if (e.getCause() instanceof IOException) {
                   throw (IOException) e.getCause();
               }
               throw new RuntimeException(e);
           }
       }

       /**
        * 执行AI生成的实际逻辑
        */
       private OpenAIResponse doGenerate(UserChatDTO userChatDTO, String model) throws IOException {
           List<UserUploadFile> userUploadFile = redisUtil.getUserUploadFile(userChatDTO);
           Boolean updated = redisUtil.updateExpirationTime(ThreadLocalUtil.get() + userChatDTO.getSessionId());
           if (!updated) {
               redisUtil.setUserUploadFile(userChatDTO, userUploadFile);
           }
           Prompt prompt = null;
           ChatResponse response = null;
           String ba64File = "";
           String[] upload = new String[1];
           List<Media> mediaList = new ArrayList<>();
           UserIdea userIdea = null;
           String generateFileName = null;
           String fileSize = null;
           Integer fileType = null;
           List<String> readMediaFileList = new ArrayList<>();
           
           if (!Constants.N_MODEL.contains(model)) {
               userIdea = getUserIdea(userChatDTO.getQuestion());
               log.info("多模态模型");
           }
           
           // 如果需要图片生成，使用并发处理
           if (userIdea != null && userIdea.getGenerateImage()) {
               return handleSimpleMultiModalTaskWithConcurrency(userChatDTO, model, userUploadFile, userIdea);
           }
           
           // 单任务处理逻辑
           if (userUploadFile != null && !userUploadFile.isEmpty()) {
               log.info("上传了文件");
               SystemMessage systemMessage = new SystemMessage("");
               for (UserUploadFile uploadFile : userUploadFile) {
                   if (uploadFile.getFileType() == 1) {
                       stringPathToMedia(uploadFile.getSrc(), mediaList);
                       readMediaFileList.add(uploadFile.getReadMediaFile().getPrompt() + "," + uploadFile.getReadMediaFile().getDesc());
                   } else {
                       systemMessage = new SystemMessage(uploadFile.getReadMediaFile().getDesc());
                   }
               }
               
               UserMessage userMessage = new UserMessage("");
               if (userIdea != null) {
                   log.info("得到用户想法");
                   if (userIdea.getGenerateVoice()) {
                       // TODO 语音生成
                       ba64File = "";
                       userMessage = new UserMessage(userChatDTO.getQuestion(), mediaList);
                   } else {
                       log.info("想法普通问题");
                       userMessage = new UserMessage(userChatDTO.getQuestion(), mediaList);
                   }
               } else {
                   log.info("无想法普通问题");
                   userMessage = new UserMessage(userChatDTO.getQuestion(), mediaList);
               }
               
               prompt = new Prompt(List.of(systemMessage, userMessage), OpenAiChatOptions.builder()
                       .withModel(setDefault(model))
                       .withHttpHeaders(Map.of("Accept-Encoding", "identity"))
                       .build());
               response = chatClient.call(prompt);
           } else {
               UserMessage userMessage = null;
               log.info("没有上传文件");
               
               if (userIdea != null) {
                   if (userIdea.getGenerateVoice()) {
                       // TODO 语音生成
                       ba64File = "";
                       userMessage = new UserMessage(userChatDTO.getQuestion());
                   } else {
                       userMessage = new UserMessage(userChatDTO.getQuestion());
                   }
               } else {
                   log.info("普通问题");
                   userMessage = new UserMessage(userChatDTO.getQuestion());
               }
               
               prompt = new Prompt(userMessage, OpenAiChatOptions
                       .builder()
                       .withModel(setDefault(model))
                       .withHttpHeaders(Map.of("Accept-Encoding", "identity"))
                       .build());
               log.info("prompt:{}", prompt.toString());
               response = chatClient.call(prompt);
           }
           
           return OpenAIResponse.builder()
                   .response(response
                           .getResult()
                           .getOutput()
                           .getContent())
                   .fileType(fileType)
                   .fileName(generateFileName)
                   .fileSize(fileSize)
                   .filePath(upload[0])
                   .build();
       }

       /**
        * 处理简单多模态任务的并发逻辑（非RAG场景）
        */
       private OpenAIResponse handleSimpleMultiModalTaskWithConcurrency(UserChatDTO userChatDTO, String model,
                                                                       List<UserUploadFile> userUploadFile, UserIdea userIdea) throws IOException {
           CountDownLatch latch = new CountDownLatch(2);
           AtomicReference<ChatResponse> textResponseRef = new AtomicReference<>();
           AtomicReference<String[]> imageUploadRef = new AtomicReference<>(new String[1]);
           AtomicReference<String> generateFileNameRef = new AtomicReference<>();
           AtomicReference<String> fileSizeRef = new AtomicReference<>();
           AtomicReference<Integer> fileTypeRef = new AtomicReference<>();
           AtomicReference<List<Media>> mediaListRef = new AtomicReference<>(new ArrayList<>());
           
           // 任务1：图像生成
           aiServiceExecutor.submit(() -> {
               try {
                   log.info("开始并发处理图像生成任务（非RAG）");
                   String ba64File;
                   if (userUploadFile != null && !userUploadFile.isEmpty()) {
                       List<String> readMediaFileList = new ArrayList<>();
                       for (UserUploadFile uploadFile : userUploadFile) {
                           if (uploadFile.getFileType() == 1) {
                               stringPathToMedia(uploadFile.getSrc(), new ArrayList<>());
                               readMediaFileList.add(uploadFile.getReadMediaFile().getPrompt() + "," + uploadFile.getReadMediaFile().getDesc());
                           }
                       }
                       String prompts = String.join(",", readMediaFileList);
                       ba64File = qianfanImageClient.getImageFromQianfanSDXL(
                               prompts + "," + userIdea.getPrompt()) + "," + userIdea.getStyle();
                   } else {
                       ba64File = qianfanImageClient.getImageFromQianfanSDXL(userIdea.getPrompt() + "," + userIdea.getStyle());
                   }
                   
                   if (ba64File.isEmpty()) {
                       return;
                   }
                   
                   String generateFileName = IdUtil.simpleUUID();
                   generateFileNameRef.set(generateFileName);
                   
                   MultipartFile multipartFile = Base64ToMultipartFileConverter.base64ToMultipartFile(ba64File, generateFileName);
                   String[] upload = aliOssUpload.upload(multipartFile);
                   imageUploadRef.set(upload);
                   
                   fileSizeRef.set(FileUtil.formatFileSize(multipartFile.getSize()));
                   fileTypeRef.set(FileUtil.getFileType(multipartFile));
                   
                   // 创建media对象
                   List<Media> mediaList = new ArrayList<>();
                   stringPathToMedia(upload[0], mediaList);
                   mediaListRef.set(mediaList);
                   
                   log.info("图像生成任务完成（非RAG）");
               } catch (Exception e) {
                   log.error("图像生成任务失败（非RAG）", e);
               } finally {
                   latch.countDown();
               }
           });
           
           // 任务2：文本处理
           aiServiceExecutor.submit(() -> {
               try {
                   log.info("开始并发处理文本任务（非RAG）");
                   // 等待图像任务完成
                   latch.countDown();
                   latch.await();
                   
                   List<Media> mediaList = mediaListRef.get();
                   UserMessage userMessage;
                   
                   if (mediaList != null && !mediaList.isEmpty()) {
                       if (userUploadFile != null && !userUploadFile.isEmpty()) {
                           userMessage = new UserMessage(Constants.FILE_TO_FILE, mediaList);
                       } else {
                           userMessage = new UserMessage(Constants.GET_FILE_CONTENT_PROMPT + " " + userChatDTO.getQuestion(), mediaList);
                       }
                   } else {
                       userMessage = new UserMessage(userChatDTO.getQuestion());
                   }
                   
                   Prompt prompt;
                   if (userUploadFile != null && !userUploadFile.isEmpty()) {
                       SystemMessage systemMessage = new SystemMessage("");
                       for (UserUploadFile uploadFile : userUploadFile) {
                           if (uploadFile.getFileType() != 1) {
                               systemMessage = new SystemMessage(uploadFile.getReadMediaFile().getDesc());
                           }
                       }
                       prompt = new Prompt(List.of(systemMessage, userMessage), OpenAiChatOptions.builder()
                               .withModel(setDefault(model))
                               .withHttpHeaders(Map.of("Accept-Encoding", "identity"))
                               .build());
                   } else {
                       prompt = new Prompt(userMessage, OpenAiChatOptions
                               .builder()
                               .withModel(setDefault(model))
                               .withHttpHeaders(Map.of("Accept-Encoding", "identity"))
                               .build());
                   }
                   
                   ChatResponse response = chatClient.call(prompt);
                   textResponseRef.set(response);
                   log.info("文本任务处理完成（非RAG）");
               } catch (Exception e) {
                   log.error("文本任务处理失败（非RAG）", e);
               }
           });
           
           // 等待所有任务完成
           try {
               latch.await();
           } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               log.error("等待任务完成时被中断", e);
           }
           
           ChatResponse response = textResponseRef.get();
           if (response == null) {
               throw new IOException("无法获取AI响应");
           }
           
           return OpenAIResponse.builder()
                   .response(response.getResult().getOutput().getContent())
                   .fileType(fileTypeRef.get())
                   .fileName(generateFileNameRef.get())
                   .fileSize(fileSizeRef.get())
                   .filePath(imageUploadRef.get()[0])
                   .build();
       }
       public Flux<ChatResponse> generateStream(UserChatDTO userChatDTO, String model) throws IOException {
           log.info("开始流式生成，用户问题: {}", userChatDTO.getQuestion());
           
           List<UserUploadFile> userUploadFile = redisUtil.getUserUploadFile(userChatDTO);
           Boolean updated = redisUtil.updateExpirationTime(ThreadLocalUtil.get() + userChatDTO.getSessionId());
           if (!updated) {
               redisUtil.setUserUploadFile(userChatDTO, userUploadFile);
           }
           
           Prompt prompt = null;
           List<Media> mediaList = new ArrayList<>();
           UserIdea userIdea = null;
           
           // 对于流式对话，我们暂时只支持文本生成，不支持多模态任务
           // 因为多模态任务（如图像生成）通常需要较长时间才能完成
           if (!Constants.N_MODEL.contains(model)) {
               userIdea = getUserIdea(userChatDTO.getQuestion());
               log.info("多模态模型，但流式只支持文本");
           }
           
           // 构建prompt的逻辑与非流式版本类似，但使用stream方法
           if (userUploadFile != null && !userUploadFile.isEmpty()) {
               log.info("上传了文件，准备流式处理");
               SystemMessage systemMessage = new SystemMessage("");
               
               for (UserUploadFile uploadFile : userUploadFile) {
                   if (uploadFile.getFileType() == 1) {
                       stringPathToMedia(uploadFile.getSrc(), mediaList);
                   } else {
                       systemMessage = new SystemMessage(uploadFile.getReadMediaFile().getDesc());
                   }
               }
               
               UserMessage userMessage = new UserMessage(userChatDTO.getQuestion(), mediaList);
               prompt = new Prompt(List.of(systemMessage, userMessage), OpenAiChatOptions.builder()
                       .withModel(setDefault(model))
                       .withHttpHeaders(Map.of("Accept-Encoding", "identity"))
                       .build());
           } else {
               log.info("没有上传文件，准备流式处理");
               UserMessage userMessage = new UserMessage(userChatDTO.getQuestion());
               prompt = new Prompt(userMessage, OpenAiChatOptions
                       .builder()
                       .withModel(setDefault(model))
                       .withHttpHeaders(Map.of("Accept-Encoding", "identity"))
                       .build());
           }
           
           log.info("准备调用stream方法获取流式响应");
           // 使用Spring AI的stream方法获取流式响应
           return chatClient.stream(prompt);
       }

       private String setDefault(String model) {
              if(model == null || model.isEmpty()) {
                    model = defaultModel;
              }
              return model;
       }
     private UserIdea getUserIdea(String prompt){
         UserIdea userIdea = jsonStructuredOutput.userIdeaOutput(prompt);
         log.info("userIdea:{}", userIdea);
         if(userIdea == null){
             userIdea = handleUserIdea(prompt);
         }
         return userIdea;
     }
    private UserIdea handleUserIdea(String idea){
            return JSONObject.parseObject("""
                    {                "generateImage":false,
                                    "generateVideo":false,
                                    "generateVoice":false,
                                    "style":"",
                                    "prompt":""
                                }""",UserIdea.class);
    }
    private void stringPathToMedia(String path,List<Media> mediaList) throws MalformedURLException {
                log.info(path);
                log.info(cn.hutool.core.io.FileUtil.getMimeType(path));
                log.info(new URL(path).toString());
                Media media = new Media(new MimeType(cn.hutool.core.io.FileUtil.getMimeType(path).split("/")[0]), new URL(path));
                mediaList.add(media);

    }


    public Flux<ChatResponse> generateRAGStream(UserChatDTO userChatDTO, String model, List<String> contents) throws IOException {
    // 获取用户ID和会话ID
    String userId = ThreadLocalUtil.get();
    String sessionId = userChatDTO.getSessionId();
    String query = userChatDTO.getQuestion();

    log.info("开始RAG流式生成，用户ID: {}, 会话ID: {}, 查询: {}", userId, sessionId, query);

    // 检查RAG缓存
    try {
        String cachedAnswer = memoryUpdateService.checkRAGCache(userId, sessionId, query);
        if (cachedAnswer != null && !cachedAnswer.isEmpty()) {
            log.info("命中RAG缓存，返回流式缓存结果，用户ID: {}, 会话ID: {}", userId, sessionId);
            return Flux.fromStream(
                            cachedAnswer.codePoints()  // 支持中文、emoji
                                    .mapToObj(cp -> String.valueOf(Character.toChars(cp))))
                    .zipWith(Flux.interval(Duration.ofMillis(30)))  // 30ms 一个字符
                    .map(tuple -> tuple.getT1())
                    .scan(new StringBuilder(), StringBuilder::append)
                    .map(sb -> {
                        // 构造 Spring AI 标准的 Generation
                        Generation generation = new Generation(sb.toString());
                        return ChatResponse.builder()
                                .withGenerations(List.of(generation))
                                .build();
                    });
        }
    } catch (Exception e) {
        log.warn("检查RAG缓存失败，继续执行RAG流式流程，用户ID: {}, 会话ID: {}", userId, sessionId, e);
    }

    // 用于收集流式响应的完整内容
    StringBuilder fullResponseBuilder = new StringBuilder();

    // 执行RAG检索流程
    return Mono.fromCallable(() -> {
        // 1. 并行检索上下文信息
        List<String> retrievalResults = parallelRetrievalService.retrieve(userId, sessionId, query, 5).get();

        // 2. 合并上下文
        String context = contextMergeService.mergeContext(retrievalResults, query);

        // 3. 构建提示词
        List<Message> messages = new ArrayList<>();
        // 添加系统提示词
        messages.add(new SystemMessage(Constants.SYSTEM_MESSAGE_PROMPT));
        // 添加上下文信息
        if (!context.isEmpty()) {
            messages.add(new SystemMessage("以下是与用户问题相关的上下文信息：\n\n" + context +
                    "\n\n请基于以上上下文信息回答用户的问题，如果上下文信息不足以回答问题，请告知用户。"));
        }

        // 4. 处理用户上传文件（如果有）
        List<UserUploadFile> userUploadFile = redisUtil.getUserUploadFile(userChatDTO);
        Boolean updated = redisUtil.updateExpirationTime(userId + sessionId);

        if (!updated) {
            redisUtil.setUserUploadFile(userChatDTO, userUploadFile);
        }

        // 5. 构建用户消息
        UserMessage userMessage;
        List<Media> mediaList = new ArrayList<>();

        // 对于流式RAG，暂时不支持多模态任务
        if (userUploadFile != null && !userUploadFile.isEmpty()) {
            log.info("上传了文件，准备RAG流式处理");

            for (UserUploadFile uploadFile : userUploadFile) {
                if (uploadFile.getFileType() == 1) {
                    stringPathToMedia(uploadFile.getSrc(), mediaList);
                }
            }

            userMessage = new UserMessage(query, mediaList);
        } else {
            log.info("没有上传文件，准备RAG流式处理");
            userMessage = new UserMessage(query);
        }

        // 添加用户消息
        messages.add(userMessage);

        // 6. 构建Prompt
        Prompt prompt = new Prompt(messages, OpenAiChatOptions.builder()
                .withModel(setDefault(model))
                .withHttpHeaders(Map.of("Accept-Encoding", "identity"))
                .build());

        return prompt;
    })
    .flatMapMany(prompt -> {
        // 7. 调用大模型流式接口
        return chatClient.stream(prompt)
                .doOnNext(chatResponse -> {
                    // 收集流式响应内容
                    if (chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                        String content = chatResponse.getResult().getOutput().getContent();
                        if (content != null) {
                            fullResponseBuilder.append(content);
                        }
                        log.debug("收到流式响应片段: {}", content);
                    }
                })
                .doOnComplete(() -> {
                    // 8. 流式完成后，更新RAG缓存和短期记忆
                    String fullAnswer = fullResponseBuilder.toString();
                    if (!fullAnswer.isEmpty()) {
                        // 异步更新短期记忆和RAG缓存
                        memoryUpdateService.updateMemoryAndCacheAsync(userId, sessionId, query, fullAnswer);
                        log.info("RAG流式响应完成，已更新缓存，用户ID: {}, 会话ID: {}", userId, sessionId);
                    }
                });
    })
    .doOnError(error -> {
        log.error("RAG流式生成出错，用户ID: {}, 会话ID: {}", userId, sessionId, error);
    });}

}
