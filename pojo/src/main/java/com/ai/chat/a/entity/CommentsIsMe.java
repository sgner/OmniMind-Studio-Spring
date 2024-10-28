package com.ai.chat.a.entity;

import com.ai.chat.a.po.Comments;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CommentsIsMe extends Comments {
    private Integer isMe;
}
