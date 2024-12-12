package com.ai.chat.a.po;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode
public class Role {
    private String agentAvatar;
    private String agentName;
    private String agentType;
    @TableId(value = "agent_id")
    private String agentId;
    private String agentDescription;
    private String agentIdentity;
    private String personalityDescription;
    private String hobby;
    private String keyPersonality;
    private String mission;
}
