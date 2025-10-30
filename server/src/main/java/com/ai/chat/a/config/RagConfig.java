package com.ai.chat.a.config;

import com.ai.chat.a.chat.openai.AAIOpenAIChatClient;
import com.ai.chat.a.properties.OpenaiChatProperties;
import com.ai.chat.a.rag.LongTermMemoryArchiveService;
import com.ai.chat.a.rag.MemoryUpdateService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * RAG功能配置类
 */
@Configuration
@EnableScheduling
@EnableAsync
public class RagConfig implements SchedulingConfigurer {

    @Resource
    private OpenaiChatProperties openaiChatProperties;
    
    @Resource
    private MemoryUpdateService memoryUpdateService;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public AAIOpenAIChatClient openAIChatClient() {
        return new AAIOpenAIChatClient(openaiChatProperties.getModel());
    }

    @Bean
    public LongTermMemoryArchiveService longTermMemoryArchiveService() {
        return new LongTermMemoryArchiveService(memoryUpdateService, stringRedisTemplate, redisConnectionFactory);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // 配置定时任务线程池
        taskRegistrar.setScheduler(taskScheduler());
    }

    @Bean(destroyMethod = "shutdown")
    public Executor taskScheduler() {
        // 创建一个专用于定时任务的线程池
        return Executors.newScheduledThreadPool(5);
    }
}