package com.ai.chat.a.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayDto {
    private String prompt; // 提示词
    private String playerId; // 玩家id
    private String agentId;
}
