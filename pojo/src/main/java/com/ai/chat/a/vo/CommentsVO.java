package com.ai.chat.a.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CommentsVO {
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
    private Boolean isLike;
}
