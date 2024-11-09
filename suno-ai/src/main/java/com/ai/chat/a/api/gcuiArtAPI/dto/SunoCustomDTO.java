package com.ai.chat.a.api.gcuiArtAPI.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class SunoCustomDTO extends SunoDTO{
    private String prompt;
    private String tags;
    private String title;

}
