package com.ai.chat.a.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GenerateLyricsResponse extends SunoResponse{
  private Data data;
  @lombok.Data
  public static class Data{
       @JsonProperty("task_id")
       private String taskId;
       private String acton;
       private String status;
       @JsonProperty("fail_reason")
       private String failReason;
       @JsonProperty("submit_time")
       private Long submitTime;
       @JsonProperty("start_time")
       private Long startTime;
       @JsonProperty("finish_time")
       private Long finishTime;
       private String progress;
       private dataInData data;
       @lombok.Data
       public static class dataInData{
           private String id;
           private String text;
           private String title;
           private String status;
       }
  }
}
