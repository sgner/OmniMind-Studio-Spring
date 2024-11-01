package com.ai.chat.a.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClickLikeVO {
     private Boolean success;
     private Boolean like;
}
