package com.ai.chat.a.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserLikeComment {
    private Integer id;
    private String userId;
    private Integer commentId;
    private String robotId;
}
