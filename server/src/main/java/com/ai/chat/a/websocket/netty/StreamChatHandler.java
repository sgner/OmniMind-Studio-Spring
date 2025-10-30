package com.ai.chat.a.websocket.netty;

import com.ai.chat.a.chat.openai.AAIOpenAIChatClient;
import com.ai.chat.a.dto.MessageDTO;
import com.ai.chat.a.dto.MessageSendDTO;
import com.ai.chat.a.dto.UserChatDTO;
import com.ai.chat.a.entity.OpenAIResponse;
import com.ai.chat.a.enums.MessageTypeEnum;
import com.ai.chat.a.po.Answers;
import com.ai.chat.a.po.Conversations;
import com.ai.chat.a.po.User;
import com.ai.chat.a.service.AnswersService;
import com.ai.chat.a.service.ConversationsService;
import com.ai.chat.a.service.SessionService;
import com.ai.chat.a.service.UserService;
import com.ai.chat.a.utils.JsonUtils;
import com.ai.chat.a.utils.RedisUtil;
import com.ai.chat.a.utils.ThreadLocalUtil;
import com.ai.chat.a.websocket.ChannelContextUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class StreamChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final AAIOpenAIChatClient aiOpenAIChatClient;
    private final UserService userService;
    private final ConversationsService conversationsService;
    private final AnswersService answersService;
    private final SessionService sessionService;
    private final RedisUtil redisUtil;
    @Qualifier("aiServiceExecutor")
    private final ExecutorService aiServiceExecutor;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        String message = frame.text();
        log.info("接收到流式对话请求: {}", message);

        try {
            // 解析请求参数
            StreamChatRequest request = JsonUtils.convertJson2Obj(message, StreamChatRequest.class);
            if (request == null || request.getUserId() == null || request.getChatDTO() == null) {
                sendErrorResponse(ctx, "无效的请求参数");
                return;
            }

            // 设置用户ID到ThreadLocal
            ThreadLocalUtil.set(request.getUserId());

            // 异步处理流式对话，避免阻塞Netty线程
            aiServiceExecutor.submit(() -> {
                try {
                    handleStreamChat(request, ctx);
                } catch (Exception e) {
                    log.error("处理流式对话失败", e);
                    sendErrorResponse(ctx, "处理流式对话失败: " + e.getMessage());
                } finally {
                    // 清除ThreadLocal
                    ThreadLocalUtil.remove();
                }
            });
        } catch (Exception e) {
            log.error("解析流式对话请求失败", e);
            sendErrorResponse(ctx, "解析请求失败: " + e.getMessage());
        }
    }

    private void handleStreamChat(StreamChatRequest request, ChannelHandlerContext ctx) throws Exception {
        UserChatDTO chatDTO = request.getChatDTO();
        String model = request.getModel() != null ? request.getModel() : "gpt-3.5-turbo"; // 默认模型
    
        // 检查是否使用RAG功能
        boolean useRAG = request.isUseRAG() != null ? request.isUseRAG() : false;
    
        // 保存用户问题到数据库
        Long conversationId = saveUserQuestion(request.getUserId(), chatDTO);
        if (conversationId == null) {
            sendErrorResponse(ctx, "保存用户问题失败");
            return;
        }
        // 生成一个唯一的响应ID
        String responseId = UUID.randomUUID().toString();
    
        // 构建流式响应的元数据
        StreamChatMetadata metadata = new StreamChatMetadata();
        metadata.setConversationId(conversationId);
        metadata.setResponseId(responseId);
        metadata.setStartTime(System.currentTimeMillis());
    
        // 发送开始响应，通知客户端流式响应即将开始
        sendStreamStartResponse(ctx, metadata);
    
        // 获取用户channel
        String userId = request.getUserId();
        String channelId = ctx.channel().id().toString();
        AttributeKey<String> attribute = AttributeKey.valueOf(channelId);
        ctx.channel().attr(attribute).set(userId);
    
        try {
            // 根据配置选择使用RAG流式响应或普通流式响应
            Flux<ChatResponse> fluxResponse;
            if (useRAG) {
                log.info("使用RAG流式对话，用户ID: {}", userId);
                fluxResponse = aiOpenAIChatClient.generateRAGStream(chatDTO, model, null);
            } else {
                log.info("使用普通流式对话，用户ID: {}", userId);
                fluxResponse = aiOpenAIChatClient.generateStream(chatDTO, model);
            }
            
            StringBuilder fullResponse = new StringBuilder();
    
            // 订阅流式响应
            fluxResponse.subscribe(
                // 处理每个响应片段
                chatResponse -> {
                    try {
                        String content = chatResponse.getResult().getOutput().getContent();
                        fullResponse.append(content);
                        
                        // 发送片段响应
                        sendStreamChunkResponse(ctx, metadata, content);
                        
                        log.info("发送流式响应片段，长度: {}", content.length());
                    } catch (Exception e) {
                        log.error("处理流式响应片段失败", e);
                    }
                },
                // 处理错误
                error -> {
                    log.error("流式响应发生错误", error);
                    sendStreamErrorResponse(ctx, metadata, "AI服务错误: " + error.getMessage());
                },
                // 处理完成
                () -> {
                    try {
                        // 发送完成响应
                        sendStreamCompleteResponse(ctx, metadata, fullResponse.toString());
                        
                        // 保存完整回答到数据库
                        saveAIAsswer(conversationId, fullResponse.toString());
                        
                        log.info("流式响应完成，总长度: {}", fullResponse.length());
                    } catch (Exception e) {
                        log.error("处理流式响应完成事件失败", e);
                    }
                }
            );
        } catch (Exception e) {
            log.error("获取流式响应失败", e);
            sendStreamErrorResponse(ctx, metadata, "获取流式响应失败: " + e.getMessage());
        }
    }

    private Long saveUserQuestion(String userId, UserChatDTO chatDTO) {
        try {
            // 保存用户问题到对话表
            Conversations conversation = new Conversations();
            conversation.setUserId(userId);
            conversation.setQuestion(chatDTO.getQuestion());
            conversation.setSessionId(chatDTO.getSessionId());
            conversation.setCreateTime(LocalDateTime.now());
            conversationsService.save(conversation);
            return conversation.getId();
        } catch (Exception e) {
            log.error("保存用户问题失败", e);
        }
        return null;
    }

    private void saveAIAsswer(Long conversationId, String answer) {
        try {
            // 保存AI回答到回答表
            Answers answers = new Answers();
            answers.setConversationId(conversationId);
            answers.setAnswer(answer);
            answers.setCreateTime(LocalDateTime.now());
            answersService.save(answers);
            
            log.info("保存AI回答到数据库，会话ID: {}", conversationId);
        } catch (Exception e) {
            log.error("保存AI回答失败", e);
        }
    }

    private void sendStreamStartResponse(ChannelHandlerContext ctx, StreamChatMetadata metadata) {
        StreamChatResponse response = new StreamChatResponse();
        response.setType("start");
        response.setMetadata(metadata);
        ctx.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(response)));
    }

    private void sendStreamChunkResponse(ChannelHandlerContext ctx, StreamChatMetadata metadata, String content) {
        StreamChatResponse response = new StreamChatResponse();
        response.setType("chunk");
        response.setMetadata(metadata);
        response.setContent(content);
        ctx.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(response)));
    }

    private void sendStreamCompleteResponse(ChannelHandlerContext ctx, StreamChatMetadata metadata, String fullContent) {
        StreamChatResponse response = new StreamChatResponse();
        response.setType("complete");
        response.setMetadata(metadata);
        response.setContent(fullContent);
        metadata.setEndTime(System.currentTimeMillis());
        ctx.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(response)));
    }

    private void sendStreamErrorResponse(ChannelHandlerContext ctx, StreamChatMetadata metadata, String errorMsg) {
        StreamChatResponse response = new StreamChatResponse();
        response.setType("error");
        response.setMetadata(metadata);
        response.setError(errorMsg);
        ctx.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(response)));
    }

    private void sendErrorResponse(ChannelHandlerContext ctx, String errorMsg) {
        StreamChatResponse response = new StreamChatResponse();
        response.setType("error");
        response.setError(errorMsg);
        ctx.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(response)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("流式对话处理器异常", cause);
        sendErrorResponse(ctx, "服务器内部错误");
        ctx.close();
    }

    // 流式对话请求类
    public static class StreamChatRequest {
        private String userId;
        private UserChatDTO chatDTO;
        private String model;
        private Boolean useRAG; // 是否使用RAG功能
        
        // getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public UserChatDTO getChatDTO() { return chatDTO; }
        public void setChatDTO(UserChatDTO chatDTO) { this.chatDTO = chatDTO; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public Boolean isUseRAG() { return useRAG; }
        public void setUseRAG(Boolean useRAG) { this.useRAG = useRAG; }
    }

    // 流式对话响应类
    public static class StreamChatResponse {
        private String type; // start, chunk, complete, error
        private StreamChatMetadata metadata;
        private String content;
        private String error;
        // getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public StreamChatMetadata getMetadata() { return metadata; }
        public void setMetadata(StreamChatMetadata metadata) { this.metadata = metadata; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    // 流式对话元数据类
    public static class StreamChatMetadata {
        private Long conversationId;
        private String responseId;
        private long startTime;
        private long endTime;
        // getters and setters
        public Long getConversationId() { return conversationId; }
        public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
        public String getResponseId() { return responseId; }
        public void setResponseId(String responseId) { this.responseId = responseId; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
    }
}