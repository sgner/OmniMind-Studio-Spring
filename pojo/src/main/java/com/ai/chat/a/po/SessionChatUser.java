package com.ai.chat.a.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionChatUser {
    private String userId;
    private String robotId;
    private Integer robotType;
    private String sessionId;
    private String robotName;
    private String lastMessage;
    private LocalDateTime lastTime;
    private Integer robotNumber;
}
