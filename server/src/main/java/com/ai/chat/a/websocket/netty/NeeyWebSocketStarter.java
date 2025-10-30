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
    private final StreamChatHandler streamChatHandler;
    @Override
    public void run() {
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(new ChannelInitializer<Channel>(){
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
                            
                            // 创建WebSocket请求路由器
                            pipeline.addLast(new WebSocketPathRouter());
                            
                            // 这里不直接添加WebSocketServerProtocolHandler，而是在路由处理器中根据路径动态添加
                            // 所有处理器都已在pipeline中，通过WebSocketPathRouter来控制使用哪个处理器
                        }
    // WebSocket路径路由器，根据路径选择使用普通对话处理器还是流式对话处理器
    private class WebSocketPathRouter extends ChannelInboundHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
                WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
                String path = complete.requestUri();
                ChannelPipeline pipeline = ctx.pipeline();
                
                // 根据路径选择不同的处理器
                if (path != null) {
                    if (path.contains("/ws/stream")) {
                        // 流式对话路径
                        log.info("客户端连接到流式对话路径: {}", path);
                        // 确保使用StreamChatHandler
                        pipeline.addLast("streamChatHandler", streamChatHandler);
                    } else {
                        // 普通对话路径（默认）
                        log.info("客户端连接到普通对话路径: {}", path);
                        // 确保使用HandlerWebSocket
                        pipeline.addLast("handler", handlerWebSocket);
                    }
                }
            }
            super.userEventTriggered(ctx, evt);
        }
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // 如果是第一个消息（通常是WebSocket握手请求），根据路径动态配置pipeline
            if (msg.toString().contains("GET /ws/stream")) {
                // 为流式对话配置WebSocket处理器
                ctx.pipeline().addLast(new WebSocketServerProtocolHandler("/ws/stream", null, true, 65536, true, true, 10000L));
                ctx.pipeline().addLast("streamChatHandler", streamChatHandler);
                log.info("配置流式对话WebSocket处理器");
            } else if (msg.toString().contains("GET /ws")) {
                // 为普通对话配置WebSocket处理器
                ctx.pipeline().addLast(new WebSocketServerProtocolHandler("/ws", null, true, 65536, true, true, 10000L));
                ctx.pipeline().addLast("handler", handlerWebSocket);
                log.info("配置普通对话WebSocket处理器");
                
                // 移除路由器，因为已经完成配置
                ctx.pipeline().remove(this);
            }
            ctx.fireChannelRead(msg);
        }
    }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(5051).sync();
            log.info("netty server start success, port:5051");
            log.info("支持的WebSocket路径：/ws (普通对话), /ws/stream (流式对话)");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e){
            log.error("启动netty失败", e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
