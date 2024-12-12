package com.ai.chat.a.api.xfXh;

import com.ai.chat.a.api.xfXh.dto.InteractiveRequest;
import com.ai.chat.a.api.xfXh.utils.InteractiveUtil;
import com.ai.chat.a.po.Player;
import com.ai.chat.a.po.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@Data
@Component
@NoArgsConstructor
@Slf4j
public class XfXhCosClient {
      private String interactiveUrl;
      private String appId;
      private String hostUrl;
      private String secret;
      private String interactionType;
      public XfXhCosClient(String interactiveUrl, String appId, String secret,String hostUrl,String interactionType){
              this.interactiveUrl = interactiveUrl;
              this.appId = appId;
              this.secret = secret;
              this.hostUrl = hostUrl;
              this.interactionType = interactionType;
              log.info("初始化角色扮演完成");
      }
     @Autowired
     private InteractiveUtil interactiveUtil;
      public void call(String agentId, String playerId, String description, String chatId, String preChatId, List<InteractiveRequest.Text> text, Role role, Player player,String sessionId,String userId) throws Exception {
          interactiveUtil.chat(interactiveUrl, appId, playerId, agentId, chatId, preChatId, text, secret,role,player,sessionId,userId);
          interactiveUtil.generate(hostUrl, secret, appId, agentId, interactionType, description,playerId,agentId);
      }
}
