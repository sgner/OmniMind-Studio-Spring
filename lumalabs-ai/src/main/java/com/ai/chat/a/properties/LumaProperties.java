package com.ai.chat.a.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "com.ai.chat.luma")
public class LumaProperties {
    private String baseUrl;
    private String apiKey;
}
