package com.ai.chat.a.api.gcuiArtAPI.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"  // 可以通过"type"字段来判断子类类型
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SunoCustomDTO.class, name = "sunoCustomDTO"),
        @JsonSubTypes.Type(value = SunoFastDTO.class,name="sunoFastDTO"),
})
public class SunoDTO {
    @JsonProperty("make_instrumental")
    private Boolean makeInstrumental = false;
    @JsonProperty("wait_audio")
    private Boolean waitAudio = false;
}
