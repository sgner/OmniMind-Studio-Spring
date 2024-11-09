package com.ai.chat.a.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenerateLyricsPromptDTO {
    private String prompt;
}
