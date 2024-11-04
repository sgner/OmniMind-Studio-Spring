package com.ai.chat.a.config;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.MilvusVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Slf4j
@Configuration
public class MilvusServiceConfig {
    @Bean
    public VectorStore vectorStore(MilvusServiceClient milvusClient, @Qualifier("openAiEmbeddingModel")EmbeddingModel embeddingModel) {
         log.info("初始化milvus vectorStore");
        MilvusVectorStore.MilvusVectorStoreConfig config = MilvusVectorStore.MilvusVectorStoreConfig.builder()
                .withCollectionName("a_ai")
                .withIndexType(IndexType.IVF_FLAT)
                .withContentFieldName("content")
                .withEmbeddingFieldName("embedding")
                .withMetadataFieldName("metadata")
                .withIDFieldName("id")
                .withEmbeddingDimension(1000)
                .withDatabaseName("A_AI")
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.COSINE)
                .build();

        return new MilvusVectorStore(milvusClient, embeddingModel, config, false, new TokenCountBatchingStrategy());
    }

    @Bean
    public MilvusServiceClient milvusClient() {
        return new MilvusServiceClient(ConnectParam.newBuilder()
                .withAuthorization("root", "123456")
                .withUri("http://192.168.71.128:19530")
                .build());
    }
}
