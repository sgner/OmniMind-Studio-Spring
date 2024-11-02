package com.ai.chat.a.api.xfXh.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: yxliu37
 * @Date: 2024/3/21 17:31
 * @Description: 玩家信息
 */
@Builder
@Data
public class PlayerDto {
    private String appId;
    private String playerId;
    private String playerName;
    private String playerType;
    private String description;
}
