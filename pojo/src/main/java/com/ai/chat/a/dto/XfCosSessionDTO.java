package com.ai.chat.a.dto;

import com.ai.chat.a.po.Player;
import com.ai.chat.a.po.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class XfCosSessionDTO {
    private Player player;
    private Role role;
}
