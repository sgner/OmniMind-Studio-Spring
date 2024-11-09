package com.ai.chat.a.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class GenerateSongResponse extends SunoResponse{
    private Data data;

    @lombok.Data
    public static class Data {
        @JsonProperty("task_id")
        private String taskId;
        private String action;
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
        private List<Item> data;

        @lombok.Data
        public static class Item {

            private String id;
            private String title;
            private String handle;
            private String status;
            @JsonProperty("user_id")
            private String userId;
            @JsonProperty("is_liked")
            private Boolean isLiked;
            private Metadata metadata;
            private String reaction;
            @JsonProperty("audio_url")
            private String audioUrl;
            @JsonProperty("image_url")
            private String imageUrl;
            @JsonProperty("is_public")
            private Boolean isPublic;
            @JsonProperty("video_url")
            private String videoUrl;
            @JsonProperty("created_at")
            private String createdAt;
            @JsonProperty("is_trashed")
            private Boolean isTrashed;
            @JsonProperty("model_name")
            private String modelName;
            @JsonProperty("play_count")
            private Integer playCount;
            @JsonProperty("display_name")
            private String displayName;
            @JsonProperty("upvote_count")
            private Integer upvoteCount;
            @JsonProperty("image_large_url")
            private String imageLargeUrl;
            @JsonProperty("is_video_pending")
            private Boolean isVideoPending;
            @JsonProperty("is_handle_updated")
            private Boolean isHandleUpdated;
            @JsonProperty("major_model_version")
            private String majorModelVersion;

            @lombok.Data
            public static class Metadata {
                private String tags;
                private String type;
                private String prompt;
                private Boolean stream;
                private String history;
                private Double duration;
                @JsonProperty("error_type")
                private String errorType;
                @JsonProperty("error_message")
                private String errorMessage;
                @JsonProperty("concat_history")
                private String concatHistory;
                @JsonProperty("refund_credits")
                private Boolean refundCredits;
                @JsonProperty("audio_prompt_id")
                private String audioPromptId;
                @JsonProperty("gpt_description_prompt")
                private String gptDescriptionPrompt;
            }
        }
    }
}
