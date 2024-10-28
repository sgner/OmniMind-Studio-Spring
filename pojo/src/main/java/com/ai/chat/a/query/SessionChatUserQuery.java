package com.ai.chat.a.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class SessionChatUserQuery extends PageQuery{
      private String userId;
      private String sessionId;
      private Integer status;
      private LocalDateTime lastMessageTime;
}
