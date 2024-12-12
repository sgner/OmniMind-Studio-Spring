package com.ai.chat.a.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {
    @TableId(value = "id")
    private String sessionId;
    private String userId;
    private String robotId;
    private Integer noReadCount;
    private Integer robotType;
    private Integer status;
    private String robotName;
    private String lastMessage;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private LocalDateTime lastTime;
    private Integer robotNumber;
    private String userName;
    private String userAvatar;
    private String robotAvatar;
}
