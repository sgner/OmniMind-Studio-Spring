package com.ai.chat.a.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenVideoSender {
      private final RabbitTemplate rabbitTemplate;
      public void sendMessageForCommit(){

      }

}
