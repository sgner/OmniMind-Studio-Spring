package com.ai.chat.a.api.lumalabsApi.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenVideoResponse {
    private String id;
    private String state;
    private String failure_reason;
    private String create_at;
    private Assets assets;
    private String version;
    private RequestResponse request;
    @Data
    public static class Assets{
        private String video;
    }
    @Data
    public static class RequestResponse{
         private String prompt;
         private String aspect_ratio;
         private Boolean loop;
         private Keyframes keyframes;

         @Data
        public static class Keyframes{
             private Frame0 frame0;
             private Frame1 frame1;
             @Data
             public static class Frame0{
                 private String type;
                 private String url;
             }
             @Data
             public static class Frame1{
                 private String type;
                 private String id;
             }
         }
    }


}
