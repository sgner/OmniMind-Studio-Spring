package com.ai.chat.a.api.aiCoreAPI.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CustomModePromptDTO extends GenerateSongPromptDTO{
    private String prompt;
    private String tags;
    private String title;
}
