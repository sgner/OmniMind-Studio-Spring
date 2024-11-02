package com.ai.chat.a.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "xf.list")
@Data
public class XfBlackAndWhiteProperties {
    private String black_list;
    private String white_list;
    private String res_id;
    private String hostUrl;
}
