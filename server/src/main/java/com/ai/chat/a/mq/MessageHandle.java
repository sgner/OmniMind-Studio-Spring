package com.ai.chat.a.mq;

import com.ai.chat.a.dto.MessageSendDTO;
import com.ai.chat.a.websocket.ChannelContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class MessageHandle {
    private final RabbitTemplate rabbitTemplate;
    private final ChannelContextUtils channelContextUtils;
    private static final String EXCHANGE_NAME = "message.fanout";
    private static final String QUEUE_NAME = "message.queue";
    public void sendMessage(MessageSendDTO message){
           log.info("推送消息");
           rabbitTemplate.convertAndSend(EXCHANGE_NAME,"",message);
    }

    @RabbitListener(bindings = @QueueBinding(
           value = @Queue(name = QUEUE_NAME,durable = "true"),
           exchange = @Exchange(name = EXCHANGE_NAME,type = ExchangeTypes.FANOUT)
    ))
    public void receiveMessage(MessageSendDTO message){
            log.info("推送到前端");
            log.info("message:{}", message.getExtendData());
            channelContextUtils.sendMessage(message);
    }
}
