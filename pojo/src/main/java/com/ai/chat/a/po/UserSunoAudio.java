package com.ai.chat.a.po;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSunoAudio {
    private String id;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("session_id")
    private String sessionId;
    private String status;
    @JsonProperty("create_time")
    private LocalDateTime createTime;
}
