package com.ai.chat.a.rag;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * 并行检索服务
 * 实现短期记忆(Redis)、长期记忆(Milvus)和知识库(Milvus)的并行检索
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParallelRetrievalService {

    private final EmbeddingCacheService embeddingCacheService;
    private final StringRedisTemplate redisTemplate;
    private final VectorStore vectorStore;
    private final ExecutorService aiServiceExecutor;
    
    // Redis键前缀
    private static final String SHORT_TERM_MEMORY_PREFIX = "memory:short:";
    private static final String RAG_CACHE_PREFIX = "rag:cache:";
    
    /**
     * 并行检索短期记忆、长期记忆和知识库
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param query 查询内容
     * @param topK 每个来源的检索数量
     * @return 合并后的检索结果（字符串列表）
     */
    public CompletableFuture<List<String>> retrieve(String userId, String sessionId, String query, int topK) {
        // 并行执行三个检索任务
        CompletableFuture<List<String>> shortTermFuture = CompletableFuture.supplyAsync(
                () -> retrieveShortTermMemory(userId, sessionId, topK), aiServiceExecutor);
        
        CompletableFuture<List<String>> longTermFuture = retrieveLongTermMemory(userId, query, topK);
        
        CompletableFuture<List<String>> knowledgeBaseFuture = retrieveKnowledgeBase(query, topK);

        return CompletableFuture.allOf(shortTermFuture, longTermFuture, knowledgeBaseFuture)
                .thenApply(v -> {
                    List<String> allResults = new ArrayList<>();
                    try {
                        // 添加短期记忆（权重最高）
                        allResults.addAll(shortTermFuture.get());
                        // 添加长期记忆
                        allResults.addAll(longTermFuture.get());
                        // 添加知识库内容
                        allResults.addAll(knowledgeBaseFuture.get());
                        
                        log.debug("并行检索完成，总共检索到{}条内容", allResults.size());
                        return allResults;
                    } catch (Exception e) {
                        log.error("合并检索结果失败", e);
                        return new ArrayList<>();
                    }
                });
    }
    
    /**
     * 检索短期记忆（从Redis）
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param topK 检索数量
     * @return 短期记忆内容列表
     */
    private List<String> retrieveShortTermMemory(String userId, String sessionId, int topK) {
        try {
            String key = SHORT_TERM_MEMORY_PREFIX + userId + ":" + sessionId;
            
            // 从Redis获取最近的对话历史，固定获取最近10条
            List<String> conversationHistory = redisTemplate.opsForList().range(key, 0, 9);
            
            if (conversationHistory == null || conversationHistory.isEmpty()) {
                log.debug("未找到用户的短期记忆，用户ID: {}, 会话ID: {}", userId, sessionId);
                return new ArrayList<>();
            }
            
            log.debug("从Redis检索到{}条短期记忆", conversationHistory.size());
            return conversationHistory;
        } catch (Exception e) {
            log.error("检索短期记忆失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 检索长期记忆（Milvus）
     * 
     * @param userId 用户ID
     * @param query 查询内容
     * @param topK 返回数量
     * @return 长期记忆列表
     */
    private CompletableFuture<List<String>> retrieveLongTermMemory(String userId, String query, int topK) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 使用VectorStore搜索用户的长期记忆
                List<Document> documents = vectorStore.similaritySearch(
                        SearchRequest.query(query)
                                .withTopK(topK)
                                .withFilterExpression("source == 'long-term-memory' && userId == '" + userId + "'")
                );
                
                List<String> memoryList = documents.stream()
                        .map(Document::getContent)
                        .collect(Collectors.toList());
                
                log.debug("检索到{}条长期记忆，用户ID: {}", memoryList.size(), userId);
                return memoryList;
            } catch (Exception e) {
                log.error("检索长期记忆失败", e);
                return new ArrayList<>();
            }
        }, aiServiceExecutor);
    }
    
    /**
     * 检索知识库（Milvus）
     * 
     * @param query 查询内容
     * @param topK 返回数量
     * @return 知识库内容列表
     */
    private CompletableFuture<List<String>> retrieveKnowledgeBase(String query, int topK) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 使用VectorStore搜索知识库
                List<Document> documents = vectorStore.similaritySearch(
                        SearchRequest.query(query)
                                .withTopK(topK)
                                .withFilterExpression("source == 'knowledge-base'")
                );
                
                List<String> knowledgeList = documents.stream()
                        .map(Document::getContent)
                        .collect(Collectors.toList());
                
                log.debug("检索到{}条知识库内容", knowledgeList.size());
                return knowledgeList;
            } catch (Exception e) {
                log.error("检索知识库失败", e);
                return new ArrayList<>();
            }
        }, aiServiceExecutor);
    }
    
    /**
     * 检索结果类
     */
    public static class RetrievalResult {
        private List<Document> shortTermMemory = new ArrayList<>();
        private List<Document> longTermMemory = new ArrayList<>();
        private List<Document> knowledgeBase = new ArrayList<>();
        
        // Getters and Setters
        public List<Document> getShortTermMemory() {
            return shortTermMemory;
        }
        
        public void setShortTermMemory(List<Document> shortTermMemory) {
            this.shortTermMemory = shortTermMemory;
        }
        
        public List<Document> getLongTermMemory() {
            return longTermMemory;
        }
        
        public void setLongTermMemory(List<Document> longTermMemory) {
            this.longTermMemory = longTermMemory;
        }
        
        public List<Document> getKnowledgeBase() {
            return knowledgeBase;
        }
        
        public void setKnowledgeBase(List<Document> knowledgeBase) {
            this.knowledgeBase = knowledgeBase;
        }
        
        /**
         * 获取所有文档
         * 
         * @return 所有文档列表
         */
        public List<Document> getAllDocuments() {
            List<Document> allDocs = new ArrayList<>();
            allDocs.addAll(shortTermMemory);
            allDocs.addAll(longTermMemory);
            allDocs.addAll(knowledgeBase);
            return allDocs;
        }
    }
}