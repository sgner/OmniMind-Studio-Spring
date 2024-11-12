package com.ai.chat.a.mq;

import com.ai.chat.a.api.aiCoreAPI.response.*;
import com.ai.chat.a.api.aiCoreAPI.util.Request;
import groovy.util.logging.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Slf4j

public class SunoAicoreProcessSender {
    private final RabbitTemplate rabbitTemplate;

    public SunoAicoreProcessSender(RabbitTemplate rabbitTemplate){
            this.rabbitTemplate = rabbitTemplate;
    }
    private final static String EXCHANGE_NAME = "suno.fanout";

    public void sendMessage(SunoResponse message){
           rabbitTemplate.convertAndSend(EXCHANGE_NAME, "", message);
    }

}
