package com.ai.chat.a.api.gcuiArtAPI.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SunoCustomDTO extends SunoDTO{
    private String prompt;
    private String tags;
    private String title;

}
