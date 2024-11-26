package com.ai.chat.a.config;

import com.ai.chat.a.api.xfXh.utils.AgentUtil;
import com.ai.chat.a.properties.XfProperties;
import com.ai.chat.a.properties.XfXhCosplayPlayerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XfXhAgentUtilConfig {
    @Bean
    public AgentUtil agentUtil(XfProperties xfProperties, XfXhCosplayPlayerProperties xfXhCosplayPlayerProperties) {
        return new AgentUtil(xfProperties.getAppid(),
                xfProperties.getApiSecret(),
                xfXhCosplayPlayerProperties.getUrl());
    }
}
