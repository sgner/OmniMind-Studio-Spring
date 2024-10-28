package com.ai.chat.a.dto;

import com.ai.chat.a.query.CommentQuery;
import com.ai.chat.a.query.PageQuery;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentDTO extends CommentQuery {
    private String content;
}
