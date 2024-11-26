package com.ai.chat.a.mq;

import cn.hutool.core.bean.BeanUtil;
import com.ai.chat.a.api.gcuiArtAPI.dto.SunoAudioResponseDTO;
import com.ai.chat.a.api.gcuiArtAPI.response.SunoAudioResponse;
import com.ai.chat.a.api.gcuiArtAPI.response.SunoLyricsResponse;
import com.ai.chat.a.api.gcuiArtAPI.util.RequestGcui;
import com.ai.chat.a.dto.MessageSendDTO;
import com.ai.chat.a.enums.MessageTypeEnum;
import com.ai.chat.a.po.UserSunoAudio;
import com.ai.chat.a.service.UserSunoAudioService;
import com.ai.chat.a.utils.ThreadLocalUtil;
import com.ai.chat.a.websocket.ChannelContextUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
@Component
@Slf4j
@RequiredArgsConstructor
public class SunoGcuiProcessReceive {
    private final RequestGcui requestGcui;
    private final ChannelContextUtils channelContextUtils;
    private final static String QUEUE_NAME = "suno.gcui.queue";
    private final static String QUEUE_NAME_LYRICS = "suno.gcui.queue.lyrics";
    private final static String QUEUE_NAME_END = "suno.gcui.queue.end";
    private final static String ROUTE_KEY_END = "end";
    private final static String ROUTE_KEY = "song";
    private final static String ROUTE_KEY_LYRICS = "lyrics";
    private final static String EXCHANGE_NAME = "suno.gcui.direct";
    private final static String EXCHANGE_NAME_END = "suno.gcui.direct.end";
    private final UserSunoAudioService userSunoAudioService;
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = QUEUE_NAME,durable = "true"),
                    exchange = @Exchange(name = EXCHANGE_NAME,type = ExchangeTypes.DIRECT),
                    key= {ROUTE_KEY}
            )
    )
    public void  receiveMessage(List<SunoAudioResponseDTO> message){
        if(!message.isEmpty()){
            List<String> ids = message.stream().map(SunoAudioResponse::getId).toList();
            log.info("收到消息：{}", JSONObject.toJSONString(message));
            log.info("开始获取生成结果");
            String id = String.join(",", ids);

            List<UserSunoAudio> userSunoAudios = BeanUtil.copyToList(message, UserSunoAudio.class);
            log.info(userSunoAudios.toString());
            try {
                userSunoAudios.forEach(userSunoAudio -> {
                    userSunoAudio.setCreateTime(LocalDateTime.now());
                    userSunoAudio.setUserId(message.get(0).getUserId());
                });
                userSunoAudioService.saveBatch(userSunoAudios);
            }catch (Exception e){
                e.printStackTrace();
                log.error("保存失败！");
            }
            SunoAudioResponseDTO sunoAudioResponseDTO = message.get(0);
            requestGcui.getGenerateSongRequest(id,0,sunoAudioResponseDTO.getUserId(),sunoAudioResponseDTO.getSessionId());
        }
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name =QUEUE_NAME_LYRICS,durable = "true"),
                    exchange = @Exchange(name = EXCHANGE_NAME,type = ExchangeTypes.DIRECT),
                    key= {ROUTE_KEY_LYRICS}
            )
    )
    public void  receiveMessage(SunoLyricsResponse message){
        log.info("收到消息：{}",JSONObject.toJSONString(message));
    }
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name =QUEUE_NAME_END,durable = "true"),
                    exchange = @Exchange(name = EXCHANGE_NAME_END,type = ExchangeTypes.DIRECT),
                    key= {ROUTE_KEY_END}
            )
    )
    public void receiveEndProcess(List<SunoAudioResponseDTO> message){
        log.info("收到结束消息：{}",JSONObject.toJSONString(message));
        List<UserSunoAudio> userSunoAudios = BeanUtil.copyToList(message, UserSunoAudio.class);
        log.info(userSunoAudios.toString());
        try{
            userSunoAudios.forEach(userSunoAudio ->{
                 userSunoAudio.setCreateTime(LocalDateTime.now());
                 userSunoAudio.setUserId(message.get(0).getUserId());
            });
            log.info(message.get(0).getUserId());
            userSunoAudioService.updateBatchById(userSunoAudios);
            log.info("完成！");
            List<UserSunoAudio> list = userSunoAudioService.list(new LambdaQueryWrapper<UserSunoAudio>().eq(UserSunoAudio::getUserId, message.get(0).getUserId()));
            List<String> ids = list.stream().map(UserSunoAudio::getId).toList();
            List<SunoAudioResponse> generatedSongRequest = requestGcui.getGeneratedSongRequest("");
            MessageSendDTO<Object> messageSendDTO = MessageSendDTO.builder()
                    .messageType(MessageTypeEnum.SUNO_AUDIO.getType())
                    .contactId(message.get(0).getUserId())
                    .extendData(generatedSongRequest)
                    .build();
            channelContextUtils.sendMessage(messageSendDTO);
        }catch (Exception e){
            e.printStackTrace();
            log.error("保存失败！");
        }

    }
}
