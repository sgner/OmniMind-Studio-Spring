package com.ai.chat.a.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserChatDTO {
    @NotEmpty
    private String question;
    private LocalDateTime createTime;
    private Integer status;
    @NotEmpty
    private Integer questionType;
    @NotEmpty
    private String sessionId;
    private String userName;
}
