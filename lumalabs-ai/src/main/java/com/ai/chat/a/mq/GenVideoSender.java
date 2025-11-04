package com.ai.chat.a.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenVideoSender {
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange.video:luma.video.exchange}")
    private String videoExchange;
    
    @Value("${rabbitmq.routing.key.video:luma.video.routing.key}")
    private String videoRoutingKey;
    
    /**
     * 发送视频生成请求到消息队列
     * @param videoParams 视频生成参数
     */
    public void sendMessageForCommit(Map<String, Object> videoParams) {
        try {
            log.info("发送视频生成请求到消息队列: {}", videoParams);
            rabbitTemplate.convertAndSend(videoExchange, videoRoutingKey, videoParams);
            log.info("视频生成请求发送成功");
        } catch (Exception e) {
            log.error("发送视频生成请求失败", e);
            throw new RuntimeException("发送视频生成请求失败: " + e.getMessage());
        }
    }
}
