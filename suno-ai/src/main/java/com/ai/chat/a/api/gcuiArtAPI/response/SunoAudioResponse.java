package com.ai.chat.a.api.gcuiArtAPI.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Data
@SuperBuilder
public class SunoAudioResponse {
     private List<ResponseMap> responseMaps;
     @Data
     public static class ResponseMap{
         private String index;
         private Item item;
     }
     @Data
     public static class Item{
         private String id;
         private String title;
         @JsonProperty("image_url")
         private String imageUrl;
         private String lyric;
         @JsonProperty("audio_url")
         private String audioUrl;
         @JsonProperty("video_url")
         private String videoUrl;
         @JsonProperty("created_at")
         private String createdAt;
         @JsonProperty("model_name")
         private String modelName;
         private String status;
         @JsonProperty("gpt_description_prompt")
         private String gptDescriptionPrompt;
         private String prompt;
         private String type;
         private String tags;
     }

}
