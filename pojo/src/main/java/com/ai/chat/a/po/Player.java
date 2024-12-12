package com.ai.chat.a.po;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Player {
   private String playerName;
   @TableId(value = "player_id")
   private String playerId;
   private String description;
   private String playerAvatar;
   private String playerType;
   private String senderIdentity;
   private String userId;
}
