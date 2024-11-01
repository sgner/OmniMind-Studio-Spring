package com.ai.chat.a.config;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.MilvusVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Configuration
public class MilvusServiceConfig {
    @Bean
    public VectorStore vectorStore(MilvusServiceClient milvusClient, @Qualifier("openAiEmbeddingModel")EmbeddingModel embeddingModel) {
         log.info("初始化milvus vectorStore");
        MilvusVectorStore.MilvusVectorStoreConfig config = MilvusVectorStore.MilvusVectorStoreConfig.builder()
                .withCollectionName("a_ai")
                .withDatabaseName("A_AI")
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.COSINE)
                .build();

        return new MilvusVectorStore(milvusClient, embeddingModel, config, true, new BatchingStrategy() {
            @Override
            public List<List<Document>> batch(List<Document> documents) {
                int batchSize = 100;
                List<List<Document>> batches = new ArrayList<>();

                for (int i = 0; i < documents.size(); i += batchSize) {
                    // 计算当前批次的结束索引
                    int end = Math.min(i + batchSize, documents.size());
                    // 从文档列表中提取当前批次
                    List<Document> batch = documents.subList(i, end);
                    batches.add(batch);
                }

                return batches;
            }
        });
    }

    @Bean
    public MilvusServiceClient milvusClient() {
        return new MilvusServiceClient(ConnectParam.newBuilder()
                .withAuthorization("root", "123456")
                .withUri("http://192.168.71.128:19530")
                .build());
    }
}
