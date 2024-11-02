package com.ai.chat.a.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "xf.xfxh.cosplay.player")
@Data
public class XfXhCosplayPlayerProperties {
    private String interactiveUrl;
    private String url;
    private String playerType;
    private String desc;

}
