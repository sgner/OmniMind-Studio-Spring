package com.ai.chat.a.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class GenerateSongPromptDTO {
    public Boolean make_instrumental;
    public String mv;
}
