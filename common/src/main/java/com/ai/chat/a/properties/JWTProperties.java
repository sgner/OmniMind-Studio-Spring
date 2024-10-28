package com.ai.chat.a.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "com.ai.chat.jwt")
@Data
public class JWTProperties {
       private String secretKey;
       private long ttl;
       private String tokenName;
       private String adminSecretKey;
}
