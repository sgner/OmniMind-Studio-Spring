package com.ai.chat.a.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserIdea {
    private Boolean generateImage;
    private Boolean generateVideo;
    private Boolean generateVoice;
    private String Style;
    private String prompt;
}
