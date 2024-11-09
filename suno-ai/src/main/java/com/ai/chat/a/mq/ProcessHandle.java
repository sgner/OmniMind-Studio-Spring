package com.ai.chat.a.mq;

import com.ai.chat.a.api.response.*;
import com.ai.chat.a.api.util.Request;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
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

public class ProcessHandle {
    private final RabbitTemplate rabbitTemplate;
    private final Request request;

    public ProcessHandle(RabbitTemplate rabbitTemplate, @Lazy Request request){
            this.rabbitTemplate = rabbitTemplate;
            this.request = request;
    }
    private final static String QUEUE_NAME = "suno.queue";
    private final static String EXCHANGE_NAME = "suno.fanout";

    public void sendMessage(SunoResponse message){
           rabbitTemplate.convertAndSend(EXCHANGE_NAME, "", message);
    }

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
