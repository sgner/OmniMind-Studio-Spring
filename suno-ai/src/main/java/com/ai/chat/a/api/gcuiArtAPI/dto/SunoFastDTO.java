package com.ai.chat.a.api.gcuiArtAPI.dto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class SunoFastDTO extends SunoDTO{
    private String prompt;
}
