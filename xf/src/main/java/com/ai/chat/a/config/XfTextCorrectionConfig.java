package com.ai.chat.a.config;

import com.ai.chat.a.api.textCorrection.XfTextCorrectionbw;
import com.ai.chat.a.api.textCorrection.XfWebTextCorrectionClient;
import com.ai.chat.a.properties.XfProperties;
import com.ai.chat.a.properties.XfTextCorrectionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XfTextCorrectionConfig {
    @Bean
    public XfWebTextCorrectionClient xfwebTextCorrectinClient(XfProperties xfProperties, XfTextCorrectionProperties xfTextCorrectionProperties, XfTextCorrectionbw xfTextCorrectionbw){
        return new XfWebTextCorrectionClient(xfProperties.getAppid(),xfProperties.getApiSecret(),xfProperties.getApiKey(),xfTextCorrectionProperties.getHostUrl(),xfTextCorrectionbw);
    }
}
