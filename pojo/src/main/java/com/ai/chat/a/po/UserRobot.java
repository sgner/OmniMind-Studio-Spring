package com.ai.chat.a.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRobot {
    private String userId;
    private String robotId;
    private Integer robotType;
    private LocalDateTime createTime;
    private Integer status;
    private LocalDateTime updateTime;
    private String robotName;
}
