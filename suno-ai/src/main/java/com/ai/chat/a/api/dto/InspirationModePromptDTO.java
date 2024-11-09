package com.ai.chat.a.api.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class InspirationModePromptDTO extends GenerateSongPromptDTO{
    private String gpt_description_prompt;
}
