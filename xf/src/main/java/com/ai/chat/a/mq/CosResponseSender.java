package com.ai.chat.a.mq;

import com.ai.chat.a.dto.CosResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CosResponseSender {
    private final RabbitTemplate rabbitTemplate;
    private final static String EXCHANGE_NAME = "cos-response-exchange";

    public void sendMessage(CosResponseDTO message){
          log.info("send message : {}",message);
          rabbitTemplate.convertAndSend(EXCHANGE_NAME,"",message);
    }

}
