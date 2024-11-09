package com.ai.chat.a.controller;

import com.ai.chat.a.chat.openai.AAIOpenAIChatClient;
import com.ai.chat.a.dto.UserChatDTO;
import com.ai.chat.a.entity.OpenAIResponse;
import com.ai.chat.a.milvus.AVectorDB;
import com.ai.chat.a.po.Session;
import com.ai.chat.a.po.UserDocument;
import com.ai.chat.a.result.R;
import com.ai.chat.a.service.SessionService;
import com.ai.chat.a.service.UserDocumentService;
import com.ai.chat.a.utils.ThreadLocalUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private AVectorDB aVectorDB;
    @PostMapping("/openai/{model}")
    public R chatWithOpenai(@RequestBody UserChatDTO userChatDTO, @PathVariable String model) throws IOException {
        log.info("用户提问"+userChatDTO);
        OpenAIResponse generate = aAiOpenAIChatClient.generate(userChatDTO, model);
        Session currentSession = sessionService.getOne(new LambdaQueryWrapper<Session>()
                .eq(Session::getSessionId,userChatDTO.getSessionId()));
        sessionService.updateSession(currentSession,userChatDTO,generate);
        return R.success();
    }

    @PostMapping("/openai/rag/{model}")
    public R chatWithOpenaiRag(@RequestBody UserChatDTO userChatDTO,@PathVariable String model) throws IOException {
         // TODO 添加记忆功能
        // TODO 完成RAG对话，解决超长文本记忆和token限制问题
        log.info("获取用户documentId");
        List<UserDocument> documents = userDocumentService.list(new LambdaQueryWrapper<UserDocument>()
                .eq(UserDocument::getSessionId, userChatDTO.getSessionId())
                .eq(UserDocument::getUserId, ThreadLocalUtil.get()));
        List<String> ids = documents.stream().map(UserDocument::getId).toList();
        log.info("从milvus搜索相关记录");
        List<String> contents = aVectorDB.searchDocumentPrompt(userChatDTO.getQuestion(), ids);
        OpenAIResponse response = aAiOpenAIChatClient.generateRAG(userChatDTO, model, contents);
        log.info("将用户提问和模型回答存入向量数据库");
        List<String> vids = aVectorDB.addDocument(List.of(userChatDTO.getQuestion(), response.getResponse()));
        List<UserDocument> userDocuments = new ArrayList<>();
        vids.forEach(id ->{
             userDocuments.add(UserDocument.builder()
                     .documentId(id)
                     .sessionId(userChatDTO.getSessionId())
                     .userId(ThreadLocalUtil.get())
                     .build());
        });
        userDocumentService.saveBatch(userDocuments);
        Session currentSession = sessionService.getOne(new LambdaQueryWrapper<Session>()
                .eq(Session::getSessionId,userChatDTO.getSessionId()));
        sessionService.updateSession(currentSession,userChatDTO,response);
        return R.success();
    }

    @PostMapping("/openai/rag/flux/{model}")
    public R chatWithOpenaiRagFlux(){
        // TODO 添加记忆功能
        // TODO 流式对话并且解决超长文本记忆和token限制问题
        return R.success();
    }
}
