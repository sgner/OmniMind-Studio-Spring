package com.ai.chat.a.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "xf")
@Data
public class XfProperties {
    private String appid;
    private String apiSecret;
    private String apiKey;
}
