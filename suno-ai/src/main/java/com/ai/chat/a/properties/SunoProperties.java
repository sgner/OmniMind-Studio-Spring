package com.ai.chat.a.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("spring.ai.openai")
@Component
@Data
public class SunoProperties {
    private String baseUrl;
    private String apiKey;
}
