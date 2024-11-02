package com.ai.chat.a.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("xf.xfxh.cosplay.interaction")
@Data
public class XfXhCosplayInteractionProperties {
     private String interactiveUrl;
     private String interactionType;
}
