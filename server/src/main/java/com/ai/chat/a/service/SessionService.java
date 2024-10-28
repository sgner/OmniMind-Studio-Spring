package com.ai.chat.a.service;

import com.ai.chat.a.dto.UserChatDTO;
import com.ai.chat.a.entity.OpenAIResponse;
import com.ai.chat.a.po.Session;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.annotation.Nullable;

public interface SessionService extends IService<Session> {
    public void updateSession4UploadFail(Session currentSession,String errorMsg);
    public void updateSession(Session currentSession, UserChatDTO userChatDTO, OpenAIResponse response);
}
