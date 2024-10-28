package com.ai.chat.a.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "com.ai.chat")
@Data
public class PageSizeProperties {
    private Integer pageSize;
}
