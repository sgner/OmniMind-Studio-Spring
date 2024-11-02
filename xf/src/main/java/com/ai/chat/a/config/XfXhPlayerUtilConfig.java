package com.ai.chat.a.config;

import com.ai.chat.a.api.xfXh.utils.PlayerUtil;
import com.ai.chat.a.properties.XfProperties;
import com.ai.chat.a.properties.XfXhCosplayPlayerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XfXhPlayerUtilConfig {
    @Bean
    public PlayerUtil playerUtil(XfXhCosplayPlayerProperties xfXhCosplayPlayerProperties, XfProperties xfProperties){
        return new PlayerUtil(
                xfXhCosplayPlayerProperties.getUrl(),
                xfProperties.getAppid(),
                xfProperties.getApiSecret(),
                xfXhCosplayPlayerProperties.getPlayerType(),
                xfXhCosplayPlayerProperties.getDesc());
    }

}
