package com.ai.chat.a.handle;

import com.ai.chat.a.constant.Constants;
import com.ai.chat.a.entity.ReadMediaFile;
import com.ai.chat.a.starategy.FileHandle.Context.FileHandlerContext;
import com.ai.chat.a.starategy.FileHandlerStrategy;
import com.ai.chat.a.utils.FFMPEGUtil;
import com.ai.chat.a.utils.FileUtil;
import com.ai.chat.a.utils.StringTools;
import com.alibaba.fastjson.JSONObject;
import lombok.Locked;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ai.chat.a.utils.FileUtil.convertMultipartFileToResource;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileHandle {
    private final OpenAiChatModel openAiChatModel;
    private final FileHandlerContext fileHandlerContext;
    public ReadMediaFile handleMedia(MultipartFile file, String model) throws IOException {
        try{
            String type = FileUtil.detectFileType(file);
            if(type.contains("image")){
                ChatResponse response = openAiChatModel
                        .call(new Prompt(List.of(new UserMessage(Constants.FILE_DESCRIPTION_PROMPT, new Media(MimeTypeUtils.parseMimeType(FileUtil.detectFileType(file)), convertMultipartFileToResource(file)))),
                                OpenAiChatOptions.builder().withModel(model).build()));
                log.info("回复："+response.getResult().getOutput().getContent());
                return JSONObject.parseObject(StringTools.extractJsonString(response.getResult().getOutput().getContent()), ReadMediaFile.class);
            }else if(type.contains("video")){
//               return  handleVideo(file,type,model);
                 // TODO 通过ai大模型处理视频而不是提取视频帧
                 return null;
            }else if(type.contains("audio")){
                //TODO 音频处理
//                return handleAudio(file.getOriginalFilename());
                //TODO 使用ai大模型处理音频
            }
            String fileString = fileHandlerContext.handleFile(type, file);
            if(fileString!=null){
                return ReadMediaFile.builder().desc(fileString).fetch(true).prompt("").type(type).build();
            }
            return null;
            }catch(Exception e){
            e.printStackTrace();
                 int statusCode = getStatusCode(e.getMessage());
                 if(statusCode==401 || statusCode == 403){
                     return ReadMediaFile.builder().desc(Constants.NO_AUTHOR).build();
                 }
                 return null;
            }
    }

    public ReadMediaFile handleVideo(MultipartFile file,String type,String model) throws IOException, InterruptedException {
        List<Media> mediaList = new ArrayList<>();
        List<MultipartFile> keyFrames = FFMPEGUtil.extractKeyFrames(file);
        for (MultipartFile keyFrame : keyFrames) {
            mediaList.add(new Media(MimeTypeUtils.parseMimeType(FileUtil.detectFileType(keyFrame)), convertMultipartFileToResource(keyFrame)));
        }
        ChatResponse response = openAiChatModel.call(new Prompt(List.of(new UserMessage(Constants.HANDLE_VIDEO_PROMPT,mediaList)),OpenAiChatOptions.builder().withModel(model).build()));
        return ReadMediaFile.builder().prompt(response.getResult().getOutput().getContent()).type(type).fetch(true).desc(JSONObject.toJSONString(keyFrames)).build();
    }
    public ReadMediaFile handleAudio(String filePath){
       return null;
    }
    private Integer getStatusCode(String error){
        Pattern pattern = Pattern.compile("(\\d{3}) -");
        Matcher matcher = pattern.matcher(error);
        if(matcher.find()){
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

}
