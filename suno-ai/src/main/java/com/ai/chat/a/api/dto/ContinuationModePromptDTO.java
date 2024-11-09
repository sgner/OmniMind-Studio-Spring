package com.ai.chat.a.api.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ContinuationModePromptDTO extends GenerateSongPromptDTO{
    private String prompt;
    private String tags;
    private String title;
    private String task_id;
    private String continue_clip_id;
    private Long continue_at;
}
