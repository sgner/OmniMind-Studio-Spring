package com.ai.chat.a.api.aiCoreAPI.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SongResponse extends SunoResponse{
    private String data;
}
