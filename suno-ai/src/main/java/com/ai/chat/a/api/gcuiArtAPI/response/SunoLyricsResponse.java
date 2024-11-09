package com.ai.chat.a.api.gcuiArtAPI.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SunoLyricsResponse {
   private String text;
   private String title;
   private String status;
}
