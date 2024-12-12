package com.ai.chat.a.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayCustomizeDto {
    private String playerId; // 玩家id
    private String agentId; // 角色id
    private String playerAvatar; // 玩家头像
    private String agentAvatar; // 角色头像
    private String playerName; // 玩家名称
    private String playerType; // 玩家类型
    private String description; // 玩家描述
    private String senderIdentity; // 玩家身份
    /**
     * 人格名称
     */
    private String agentName; // 角色名称
    /**
     * 人格类型
     */
    private String agentType; // 角色类型
    /**
     * 人格描述
     */
    private String agentDescription; // 角色描述

    /**
     * 性格描述
     */
    private String personalityDescription;// 角色人格描述

    /**
     * 社会身份
     */
    private String agentIdentity; // 社会身份

    /**
     * 爱好
     */
    private String hobby;  // 爱好

    /**
     * 发音人
     */
    private String speaker; // 说话人

    /**
     * 对话场景
     */
    private String openingIntroduction; // 场景介绍
    private String keyPersonality;
    private String mission;
}
