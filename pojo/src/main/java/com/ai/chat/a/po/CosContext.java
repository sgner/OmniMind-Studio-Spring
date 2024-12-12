package com.ai.chat.a.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CosContext {
    private Long id;
    private String chatId;
    private String text;
    private String userId;
    private String roleId;
    private String playerId;
}
