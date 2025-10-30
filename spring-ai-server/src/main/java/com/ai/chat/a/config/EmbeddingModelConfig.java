package com.ai.chat.a.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * EmbeddingModel配置类
 * 解决多个EmbeddingModel实现导致的依赖注入冲突问题
 */
@Configuration
public class EmbeddingModelConfig {

    /**
     * 将OpenAI EmbeddingModel标记为主要实现
     * 
     * @param openAiEmbeddingModel OpenAI EmbeddingModel
     * @return 主要的EmbeddingModel实现
     */
    @Bean
    @Primary
    public EmbeddingModel primaryEmbeddingModel(EmbeddingModel openAiEmbeddingModel) {
        return openAiEmbeddingModel;
    }
}