package com.ai.chat.a.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPlayerAgent {
     private String userId;
     private String playerId;
     private String agentId;
     private String description;
     private String playerName;
     private String agentName;
     private String sessionId;
     private String playerAvatar;
     private String agentAvatar;
}
