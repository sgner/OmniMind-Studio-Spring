package com.ai.chat.a.websocket.netty;

import com.ai.chat.a.utils.RedisUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class HandlerHeartBeat extends ChannelDuplexHandler {
    private final RedisUtil redisUtil;
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt){
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            if(event.state() == IdleState.READER_IDLE){
                Channel channel = ctx.channel();
                Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
                String userId = attribute.get();
                log.info("用户ID:{}心跳超时",userId);
                if(userId !=null){
                    redisUtil.removeUserHeartBeat(userId);
                }
                ctx.close();
            }else if(event.state() == IdleState.WRITER_IDLE){
                ctx.writeAndFlush("heart");
            }
        }
    }

}
