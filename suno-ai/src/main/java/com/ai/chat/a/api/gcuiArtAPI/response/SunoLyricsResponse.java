package com.ai.chat.a.api.gcuiArtAPI.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SunoLyricsResponse {
   private String text;
   private String title;
   private String status;
}
