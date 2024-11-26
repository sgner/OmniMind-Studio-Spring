package com.ai.chat.a.api.gcuiArtAPI.dto;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.context.annotation.Bean;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SunoFastDTO extends SunoDTO{
    private String prompt;
}
