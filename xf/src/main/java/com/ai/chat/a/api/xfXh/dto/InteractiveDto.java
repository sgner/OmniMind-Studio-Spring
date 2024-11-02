package com.ai.chat.a.api.xfXh.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: yxliu37
 * @Date: 2024/3/22 17:42
 * @Description: 交互相关dto
 */
@Data
@Builder
public class InteractiveDto {
    String appId;
    String agentId;
    String playerId;
    String chatId;
    String interactionType;
    String description;

    private String playerInvolved;
    private String agentInvolved;
}
