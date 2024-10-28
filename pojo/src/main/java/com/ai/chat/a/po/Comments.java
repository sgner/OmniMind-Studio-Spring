package com.ai.chat.a.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Comments{
    private Integer id;
    private String content;
    private Integer type;
    private LocalDateTime createTime;
    private String  userId;
    private String username;
    private String avatar;
    private String robotId;
    @TableField("`like`")
    private Integer like;
}
