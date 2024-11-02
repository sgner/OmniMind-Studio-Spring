package com.ai.chat.a.config;

import com.ai.chat.a.api.xfXh.XfXhCosClient;
import com.ai.chat.a.properties.XfProperties;
import com.ai.chat.a.properties.XfXhCosplayInteractionProperties;
import com.ai.chat.a.properties.XfXhCosplayPlayerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XfXhInteractionConfig {
    @Bean
    public XfXhCosClient xfXhCosClient(XfProperties xfProperties, XfXhCosplayInteractionProperties xfXhCosplayInteractionProperties, XfXhCosplayPlayerProperties xfXhCosplayPlayerProperties) {
        return new XfXhCosClient(xfXhCosplayInteractionProperties.getInteractiveUrl()
                ,xfProperties.getAppid()
                ,xfProperties.getApiSecret()
                ,xfXhCosplayPlayerProperties.getUrl()
                ,"学生遇到了问题，作为助教进行辅导"
                ,xfXhCosplayInteractionProperties.getInteractionType());
    }
}
