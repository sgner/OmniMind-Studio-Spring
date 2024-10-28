package com.ai.chat.a.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUploadFile implements Serializable {
    private String src;
    private String userId;
    private String fileSize;
    private String name;
    private String sessionId;
    private Integer fileType;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private LocalDateTime uploadTime;
    private String fileId;
    private Integer status;
    private ReadMediaFile readMediaFile;
}
