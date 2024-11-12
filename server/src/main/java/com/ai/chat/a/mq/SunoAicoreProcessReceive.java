package com.ai.chat.a.mq;

import com.ai.chat.a.api.aiCoreAPI.response.*;
import com.ai.chat.a.api.aiCoreAPI.util.Request;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SunoAicoreProcessReceive {
    private final Request request;
    private final static String QUEUE_NAME = "suno.queue";
    private final static String EXCHANGE_NAME = "suno.fanout";
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = QUEUE_NAME,durable = "true"),
                    exchange = @Exchange(name = EXCHANGE_NAME,type = ExchangeTypes.FANOUT)
            )
    )
    public void receiveMessage(SunoResponse message){
        if(message instanceof GenerateSongResponse){

        }else if(message instanceof GenerateLyricsResponse){
            String title = ((GenerateLyricsResponse) message).getData().getData().getTitle();
            String Lyrics = ((GenerateLyricsResponse) message).getData().getData().getText();
            System.out.println("------------------------------------------------------------");
            System.out.println(title + "\n"+ Lyrics);
            System.out.println("------------------------------------------------------------");
        }else if(message instanceof SongResponse) {
            String processId = ((SongResponse) message).getData();
            System.out.println("任务id: "+processId);
            request.generateSongRequest(processId);
        }else if( message instanceof LyricsResponse){
            String processId = ((LyricsResponse) message).getData();
            System.out.println("任务id: "+processId);
            request.generateLyricsRequest(processId);
        }
    }
}
