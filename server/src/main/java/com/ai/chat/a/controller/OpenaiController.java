package com.ai.chat.a.controller;

import com.ai.chat.a.chat.openai.AAIOpenAIChatClient;
import com.ai.chat.a.dto.UserChatDTO;
import com.ai.chat.a.entity.OpenAIResponse;
import com.ai.chat.a.po.Session;
import com.ai.chat.a.result.R;
import com.ai.chat.a.service.SessionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/chat")
@Slf4j
public class OpenaiController {
    @Resource
    private AAIOpenAIChatClient aAiOpenAIChatClient;
    @Resource
    private SessionService sessionService;
    @PostMapping("/openai/{model}")
    public R chatWithOpenai(@RequestBody UserChatDTO userChatDTO, @PathVariable String model) throws IOException {
        log.info("用户提问"+userChatDTO);
        OpenAIResponse generate = aAiOpenAIChatClient.generate(userChatDTO, model);
        Session currentSession = sessionService.getOne(new LambdaQueryWrapper<Session>()
                .eq(Session::getSessionId,userChatDTO.getSessionId()));
        sessionService.updateSession(currentSession,userChatDTO,generate);
        return R.success();
    }
}
