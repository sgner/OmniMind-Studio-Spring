package com.ai.chat.a.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "xf.xfxh.cosplay.agent")
public class XfXhCosplayAgentProperties {
    private String name;
    private String type;
    private String desc;
}
