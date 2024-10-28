package com.ai.chat.a.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAIResponse {
    private String response;
    private String filePath;
    private String fileName;
    private Integer fileType;
    private String fileSize;
}
