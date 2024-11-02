package com.ai.chat.a.config;

import com.ai.chat.a.api.textCorrection.XfTextCorrectionbw;
import com.ai.chat.a.properties.XfBlackAndWhiteProperties;
import com.ai.chat.a.properties.XfProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class XfBlackListAndWhiteListUploadConfig {
    @Bean
    public XfTextCorrectionbw xfTextCorrectionbw(XfBlackAndWhiteProperties xfBlackAndWhiteProperties, XfProperties xfProperties) {
        return new XfTextCorrectionbw(xfBlackAndWhiteProperties.getBlack_list(),xfBlackAndWhiteProperties.getWhite_list(),xfBlackAndWhiteProperties.getRes_id(),xfBlackAndWhiteProperties.getHostUrl(),xfProperties.getAppid());
    }

}
