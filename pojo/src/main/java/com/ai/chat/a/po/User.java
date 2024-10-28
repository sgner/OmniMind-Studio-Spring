package com.ai.chat.a.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @TableId(type=IdType.ASSIGN_ID)
    private String id;
    private String username;
    private String account;
    private String email;
    private String password;
    private String avatar;
    private LocalDateTime createTime;
    private String level;
    private String salt;
    private Integer status;
    private LocalDateTime lastOffTime;
}
