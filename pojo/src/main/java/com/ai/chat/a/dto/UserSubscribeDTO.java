package com.ai.chat.a.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserSubscribeDTO {
   private String robotId;
   private LocalDateTime endTime;
}
