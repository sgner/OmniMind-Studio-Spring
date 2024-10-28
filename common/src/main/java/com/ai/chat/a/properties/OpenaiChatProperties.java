package com.ai.chat.a.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "com.ai.chat.openai")
@Component
@Data
public class OpenaiChatProperties {
      private String model;
}
