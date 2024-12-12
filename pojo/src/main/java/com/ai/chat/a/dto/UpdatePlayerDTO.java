package com.ai.chat.a.dto;

import com.ai.chat.a.po.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePlayerDTO {
    private List<PlayerQueryDTO> playerQueries;
    private Player currentPlayer;
}
