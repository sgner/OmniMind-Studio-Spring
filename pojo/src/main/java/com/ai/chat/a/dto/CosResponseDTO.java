package com.ai.chat.a.dto;

import com.ai.chat.a.po.Player;
import com.ai.chat.a.po.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CosResponseDTO {
    private Role role;
    private Player player;
    private String message;
    private String sessionId;
    private String userId;
    private String context;
}
