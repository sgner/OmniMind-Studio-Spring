package com.ai.chat.a.entity;

import com.ai.chat.a.dto.MessageDTO;
import com.ai.chat.a.po.SessionChatUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSessionData {
   private SessionChatUser sessionChatUser;
   private MessageDTO messageDTO;
}
