package com.ai.chat.a.websocket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NeeyWebSocketStarter implements Runnable {
    private static EventLoopGroup bossGroup = new NioEventLoopGroup();
    private static EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final HandlerWebSocket handlerWebSocket;
    private final HandlerHeartBeat handlerHeartBeat;
    @Override
    public void run() {
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(new ChannelInitializer(){

                        @Override
                        protected void initChannel(Channel channel) throws Exception {

                            ChannelPipeline pipeline = channel.pipeline();
                            // 重要的处理器
                            // 对http协议的支持，使用http的编码器，解码器
                            pipeline.addLast(new HttpServerCodec());
                            // 聚合解码 httpRequest/httpContent/lastHttpContent到fullHttpRequest
                            //保证接收的http请求是完整的
                            pipeline.addLast(new HttpObjectAggregator(64*1024));
                            pipeline.addLast(new IdleStateHandler(6,0,0));
                            pipeline.addLast(handlerHeartBeat);
                            // http升级到支持websocket
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws",null,true,65536,true,true,10000L));
                            pipeline.addLast(handlerWebSocket);
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(5051).sync();
            log.info("netty server start success, port:5051");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e){
            log.error("启动netty失败", e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
