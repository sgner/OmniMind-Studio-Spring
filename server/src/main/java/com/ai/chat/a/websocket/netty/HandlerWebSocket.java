package com.ai.chat.a.websocket.netty;

import com.ai.chat.a.constant.JwtClaimsConstant;
import com.ai.chat.a.po.User;
import com.ai.chat.a.properties.JWTProperties;
import com.ai.chat.a.service.UserService;
import com.ai.chat.a.utils.JWTUtils;
import com.ai.chat.a.utils.RedisUtil;
import com.ai.chat.a.websocket.ChannelContextUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.net.SocketException;
import java.time.LocalDateTime;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final StringRedisTemplate redisTemplate;
    private final ChannelContextUtils channelContextUtils;
    private final JWTProperties jwtProperties;
    private final RedisUtil redisUtil;
    private final UserService userService;
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFramex) throws Exception {
        Channel channel = channelHandlerContext.channel();
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
//        log.info("收到userId{}消息：{}",userId, textWebSocketFramex.text());
        redisUtil.setUserHeartBeat(userId);

    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的连接加入..........");
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("连接断开.............");
        Channel channel = ctx.channel();
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        if(userId !=null){
            redisUtil.removeUserHeartBeat(userId);
        }
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId,userId).set(User::getLastOffTime, LocalDateTime.now()));
        channel.close();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 如果异常是 Connection reset，则忽略处理
        if (cause instanceof SocketException && "Connection reset".equals(cause.getMessage())) {
            // 可以选择记录日志或忽略
            log.info("客户端断开连接: " + ctx.channel().remoteAddress());
        } else {
            // 其他异常则继续处理
            cause.printStackTrace();
            Channel channel = ctx.channel();
            Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
            String userId = attribute.get();
            if(userId !=null){
                redisUtil.removeUserHeartBeat(userId);
            }
            ctx.close();
        }
    }
    public void userEventTriggered(ChannelHandlerContext ctx,Object evt) throws Exception{
         if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
             WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete)evt;
             String url = complete.requestUri();
             log.info("url:{}", url);
             String token = "";
             if (url.contains("token")&& url.contains("=") && url.contains("?")){
                 token = url.substring(url.lastIndexOf("=") + 1);
             }
             if (token.isEmpty()){
                 ctx.channel().close();
                 return;
             }
             String s = redisTemplate.opsForValue().get(token);
             if (s == null){
                 ctx.channel().close();
                 return;
             }
             Claims claims = null;
             try{
                 claims = JWTUtils.parseJwt(jwtProperties.getSecretKey(), s);
             }catch (ExpiredJwtException e){
                 ctx.channel().close();
             }
             if (claims != null){
                 channelContextUtils.addContext((String) claims.get(JwtClaimsConstant.USER_ID), ctx.channel());
             }
         }
    }

}
