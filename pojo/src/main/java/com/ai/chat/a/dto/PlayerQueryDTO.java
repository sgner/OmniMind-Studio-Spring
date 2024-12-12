package com.ai.chat.a.dto;

import com.ai.chat.a.po.Player;
import com.ai.chat.a.po.Role;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlayerQueryDTO {
    private Player player;
    private List<Role> roles;
}
