package com.ai.chat.a.api.xfXh;

import com.ai.chat.a.api.xfXh.dto.InteractiveRequest;
import com.ai.chat.a.api.xfXh.utils.InteractiveUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
      private static List<InteractiveRequest.Text> text = new ArrayList<>();
      private static String chatId = UUID.randomUUID().toString().substring(0, 10);
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
      public String call(String agentId,String playerId,String prompt,String description,String role) throws Exception {
          String preChatId =chatId;
          InteractiveRequest.Text text1 = new InteractiveRequest.Text();
          text1.setContent(prompt);
          text1.setRole(role);
          text.add(text1);
          chatId = UUID.randomUUID().toString().substring(0, 10);
          StringBuffer response = interactiveUtil.chat(interactiveUrl, appId, playerId, agentId, chatId, preChatId, text, secret);
          interactiveUtil.generate(hostUrl, secret, appId, agentId, interactionType, description,playerId,agentId);
          return response.toString();
      }
}
