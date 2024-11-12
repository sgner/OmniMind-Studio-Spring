package com.ai.chat.a.mq;

import com.ai.chat.a.api.gcuiArtAPI.dto.SunoAudioResponseDTO;
import com.ai.chat.a.api.gcuiArtAPI.response.SunoAudioResponse;
import com.ai.chat.a.api.gcuiArtAPI.response.SunoLyricsResponse;
import com.ai.chat.a.api.gcuiArtAPI.util.RequestGcui;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SunoGcuiProcessSender {
    private final RabbitTemplate rabbitTemplate;
    public SunoGcuiProcessSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    private final static String ROUTE_KEY_END = "end";
    private final static String ROUTE_KEY = "song";
    private final static String ROUTE_KEY_LYRICS = "lyrics";
    private final static String EXCHANGE_NAME = "suno.gcui.direct";
    private final static String EXCHANGE_NAME_END = "suno.gcui.direct.end";
    public void sendMessage(List<SunoAudioResponseDTO> message){
        log.info("请求生成歌曲任务结束");
        log.info("收到响应：{}",JSONObject.toJSONString(message));
         rabbitTemplate.convertAndSend(EXCHANGE_NAME,ROUTE_KEY,message);
    }
public void endProcess(List<SunoAudioResponseDTO> message){
         log.info("歌曲生成结束，向数据库存储数据");
         rabbitTemplate.convertAndSend(EXCHANGE_NAME_END,ROUTE_KEY_END,message);
}
    public void sendMessage(SunoLyricsResponse message){
         log.info("歌曲生成完成");
         log.info("收到响应：{}",JSONObject.toJSONString(message));
         rabbitTemplate.convertAndSend(EXCHANGE_NAME,ROUTE_KEY_LYRICS,message);
    }

}
