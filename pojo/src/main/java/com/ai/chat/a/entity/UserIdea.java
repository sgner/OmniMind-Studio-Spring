package com.ai.chat.a.entity;

import lombok.Data;

@Data
public class UserIdea {
    private Boolean generateImage;
    private Boolean generateVideo;
    private Boolean generateVoice;
    private String Style;
    private String prompt;
}
