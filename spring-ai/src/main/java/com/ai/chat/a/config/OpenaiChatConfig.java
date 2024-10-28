package com.ai.chat.a.config;

import com.ai.chat.a.chat.openai.AAIOpenAIChatClient;
import com.ai.chat.a.properties.OpenaiChatProperties;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenaiChatConfig {
     @Resource
     private OpenaiChatProperties openaiChatProperties;

      @Bean
      public AAIOpenAIChatClient openAIChatClient(){
           return new AAIOpenAIChatClient(openaiChatProperties.getModel());
      }
}
