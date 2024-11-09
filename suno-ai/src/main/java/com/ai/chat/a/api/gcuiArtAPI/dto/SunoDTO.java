package com.ai.chat.a.api.gcuiArtAPI.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class SunoDTO {
    @JsonProperty("make_instrumental")
    private Boolean makeInstrumental = false;
    @JsonProperty("wait_audio")
    private Boolean waitAudio = false;
}
