package com.ai.chat.a.api.gcuiArtAPI.dto;

import com.ai.chat.a.api.gcuiArtAPI.response.SunoAudioResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SunoAudioResponseDTO extends SunoAudioResponse {
        private String userId;
        private String sessionId;
}
