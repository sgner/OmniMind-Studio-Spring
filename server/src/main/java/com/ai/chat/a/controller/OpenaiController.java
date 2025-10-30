package com.ai.chat.a.controller;

import com.ai.chat.a.chat.openai.AAIOpenAIChatClient;
import com.ai.chat.a.dto.UserChatDTO;
import com.ai.chat.a.entity.OpenAIResponse;
import com.ai.chat.a.enums.ErrorCode;
import com.ai.chat.a.po.Session;
import com.ai.chat.a.po.UserDocument;
import com.ai.chat.a.result.R;
import com.ai.chat.a.service.SessionService;
import com.ai.chat.a.service.UserDocumentService;
import com.ai.chat.a.utils.ThreadLocalUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@RestController
@RequestMapping("/chat")
@Slf4j
public class OpenaiController {
    @Resource
    private AAIOpenAIChatClient aAiOpenAIChatClient;
    @Resource
    private SessionService sessionService;
    @Resource
    private UserDocumentService userDocumentService;
    @Resource
    private VectorStore vectorStore;
    @Resource
    private DocumentTransformer documentTransformer;
    @Resource
    @Qualifier("aiServiceExecutor")
    private ExecutorService aiServiceExecutor;
    @PostMapping("/openai/{model}")
    public R chatWithOpenai(@RequestBody UserChatDTO userChatDTO, @PathVariable String model) throws IOException {
        log.info("用户提问: {}", userChatDTO);
        try {
            
            // 异步处理AI生成和会话更新
            aiServiceExecutor.submit(() -> {
                try {
                    // 调用AI生成方法
                    OpenAIResponse generate = aAiOpenAIChatClient.generate(userChatDTO, model);
                    
                    // 更新会话信息（包括WebSocket推送）
                    Session currentSession = sessionService.getOne(new LambdaQueryWrapper<Session>()
                            .eq(Session::getSessionId, userChatDTO.getSessionId()));
                    if (currentSession != null) {
                        sessionService.updateSession(currentSession, userChatDTO, generate);
                    }
                } catch (Exception e) {
                    log.error("AI对话处理失败", e);
                    // TODO 通过WebSocket推送错误消息给前端
                }
            });
            
            return R.success("请求已接收，正在处理中...");
        } catch (Exception e) {
            log.error("提交AI对话请求失败", e);
            return R.error(ErrorCode.SYSTEM_ERROR,"提交请求失败，请稍后重试");
        }
    }

    @PostMapping("/openai/rag/{model}")
    public R chatWithOpenaiRag(@RequestBody UserChatDTO userChatDTO, @PathVariable String model) throws IOException {
        try {
            // 1. 获取用户相关文档ID（可以考虑异步，但这里先保持同步以确保正确性）
            List<UserDocument> documents = userDocumentService.list(new LambdaQueryWrapper<UserDocument>()
                    .eq(UserDocument::getSessionId, userChatDTO.getSessionId())
                    .eq(UserDocument::getUserId, ThreadLocalUtil.get()));
            List<String> ids = documents.stream().map(UserDocument::getDocumentId).toList();
            log.info("从VectorStore搜索相关记录，文档数量: {}", ids.size());
            
            // 2. 向量搜索获取相关内容
            List<Document> searchResults = vectorStore.similaritySearch(
                    SearchRequest.query(userChatDTO.getQuestion())
                            .withTopK(5)
                            .withFilterExpression("id in [" + String.join(",", ids.stream().map(id -> "'" + id + "'").toList()) + "]")
            );
            List<String> contents = searchResults.stream().map(Document::getContent).toList();

            
            // 3. 异步保存对话到向量数据库和更新会话，提高响应速度
            aiServiceExecutor.submit(() -> {
                try {
                    log.info("异步将用户提问和模型回答存入向量数据库");
                    OpenAIResponse response = aAiOpenAIChatClient.generateRAG(userChatDTO, model, contents);
                    // 创建文档对象
                    List<Document> newDocuments = new ArrayList<>();
                    
                    // 添加用户问题文档
                    Document questionDoc = new Document(userChatDTO.getQuestion());
                    questionDoc.getMetadata().put("userId", ThreadLocalUtil.get());
                    questionDoc.getMetadata().put("sessionId", userChatDTO.getSessionId());
                    questionDoc.getMetadata().put("type", "question");
                    questionDoc.getMetadata().put("timestamp", System.currentTimeMillis());
                    newDocuments.add(questionDoc);
                    
                    // 添加AI回答文档
                    Document answerDoc = new Document(response.getResponse());
                    answerDoc.getMetadata().put("userId", ThreadLocalUtil.get());
                    answerDoc.getMetadata().put("sessionId", userChatDTO.getSessionId());
                    answerDoc.getMetadata().put("type", "answer");
                    answerDoc.getMetadata().put("timestamp", System.currentTimeMillis());
                    newDocuments.add(answerDoc);
                    
                    // 处理并添加到VectorStore
                    List<Document> processedDocuments = documentTransformer.apply(newDocuments);
                    vectorStore.add(processedDocuments);
                    
                    // 保存文档ID到数据库
                    List<UserDocument> userDocuments = new ArrayList<>();
                    processedDocuments.forEach(doc -> {
                        userDocuments.add(UserDocument.builder()
                                .documentId(doc.getId())
                                .sessionId(userChatDTO.getSessionId())
                                .userId(ThreadLocalUtil.get())
                                .build());
                    });
                    userDocumentService.saveBatch(userDocuments);
                    
                    // 更新会话信息
                    Session currentSession = sessionService.getOne(new LambdaQueryWrapper<Session>()
                            .eq(Session::getSessionId, userChatDTO.getSessionId()));
                    if (currentSession != null) {
                        sessionService.updateSession(currentSession, userChatDTO, response);
                    }
                } catch (Exception e) {
                    log.error("保存对话到向量数据库或更新会话失败", e);
                }
            });
            
            return R.success("请求已接收，正在处理中...");
        } catch (Exception e) {
            log.error("RAG对话处理失败", e);
            return R.error(ErrorCode.SYSTEM_ERROR,"RAG处理失败，请稍后重试");
        }
    }

//    @PostMapping("/openai/rag/flux/{model}")
//    public R chatWithOpenaiRagFlux(){
//        // TODO 添加记忆功能
//        // TODO 流式对话并且解决超长文本记忆和token限制问题
//        return R.success();
//    }
}
