package com.ai.chat.a.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerInfoDto {
     private String playerId; // 玩家id
     private String agentId; // 角色id
}
