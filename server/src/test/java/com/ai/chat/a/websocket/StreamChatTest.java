package com.ai.chat.a.websocket;

import com.ai.chat.a.chat.openai.AAIOpenAIChatClient;
import com.ai.chat.a.dto.UserChatDTO;
import com.ai.chat.a.websocket.netty.StreamChatHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * 流式对话功能测试
 */
@Slf4j
@SpringBootTest
public class StreamChatTest {

    @Autowired
    private AAIOpenAIChatClient aiOpenAIChatClient;

    /**
     * 测试流式对话生成功能
     */
    @Test
    public void testStreamGeneration() {
        // 创建测试请求
        UserChatDTO chatDTO = new UserChatDTO();
        chatDTO.setQuestion("请简要介绍一下Spring Boot框架的主要特点");
        chatDTO.setSessionId("test-session-" + System.currentTimeMillis());
        
        // 获取流式响应
        Flux<ChatResponse> flux = aiOpenAIChatClient.generateStream(chatDTO, "gpt-3.5-turbo");
        
        // 验证流式响应
        StepVerifier.create(flux)
                .expectNextMatches(response -> {
                    String content = response.getResult().getOutput().getContent();
                    log.info("收到流式响应片段: {}", content);
                    return content != null && !content.isEmpty();
                })
                .consumeNextWith(response -> {
                    String content = response.getResult().getOutput().getContent();
                    log.info("收到更多流式响应片段: {}", content);
                })
                .thenCancel() // 由于可能返回多个片段，这里我们测试前几个后取消
                .verify(Duration.ofSeconds(10));
    }
}