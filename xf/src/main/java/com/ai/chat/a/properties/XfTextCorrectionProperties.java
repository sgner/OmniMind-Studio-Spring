package com.ai.chat.a.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "xf.text.correction")
@Data
public class XfTextCorrectionProperties {
    private String hostUrl;
}
