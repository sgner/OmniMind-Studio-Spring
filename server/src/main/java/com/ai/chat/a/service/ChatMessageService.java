package com.ai.chat.a.service;

import com.ai.chat.a.dto.UserChatDTO;

public interface ChatMessageService {
    public void sendMessage(UserChatDTO userChatDTO,String generated);
}
