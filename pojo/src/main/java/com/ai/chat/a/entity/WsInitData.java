package com.ai.chat.a.entity;

import com.ai.chat.a.dto.MessageDTO;
import com.ai.chat.a.po.SessionChatUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WsInitData {
    private List<SessionChatUser> sessionList;
    private List<MessageDTO> messageList;
}
