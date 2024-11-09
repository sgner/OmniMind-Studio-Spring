package com.ai.chat.a.api.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"  // 可以通过"type"字段来判断子类类型
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LyricsResponse.class, name = "lyricsResponse"),  // 根据实际子类添加
        @JsonSubTypes.Type(value = SongResponse.class,name="songResponse"),
        @JsonSubTypes.Type(value = GenerateSongResponse.class,name="generateSongResponse"),
        @JsonSubTypes.Type(value = GenerateLyricsResponse.class,name="generateLyricsResponse")
})
public class SunoResponse {
    private String code;
    private String message;
}
