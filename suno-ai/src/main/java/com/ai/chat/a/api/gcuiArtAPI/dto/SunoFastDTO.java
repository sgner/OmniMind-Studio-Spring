package com.ai.chat.a.api.gcuiArtAPI.dto;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.context.annotation.Bean;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class SunoFastDTO extends SunoDTO{
    private String prompt;
}
