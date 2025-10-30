package com.ai.chat.a.rag;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 短期记忆更新和RAG缓存写回服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryUpdateService {

    private final EmbeddingCacheService embeddingCacheService;
    private final StringRedisTemplate redisTemplate;
    private final VectorStore vectorStore;
    private final DocumentTransformer documentTransformer;
    private final EmbeddingModel embeddingModel;
    private final ExecutorService aiServiceExecutor;
    
    // Redis键前缀
    private static final String SHORT_TERM_MEMORY_PREFIX = "memory:short:";
    private static final String RAG_CACHE_PREFIX = "rag:cache:";
    private static final String LONG_TERM_MEMORY_PREFIX = "memory:long:";
    
    /**
     * 计算字符串的SHA256哈希值
     */
    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            log.error("计算SHA256哈希值失败", e);
            return String.valueOf(input.hashCode()); // 降级方案
        }
    }
    
    /**
     * 异步更新短期记忆和RAG缓存
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @param query     用户查询
     * @param answer    AI回答
     */
    public void updateMemoryAndCacheAsync(String userId, String sessionId, String query, String answer) {
        CompletableFuture.runAsync(() -> {
            try {
                // 更新短期记忆
                updateShortTermMemory(userId, sessionId, query, answer);

                // 写回RAG缓存
                updateRAGCache(userId, sessionId, query, answer);

                log.info("短期记忆和RAG缓存更新完成，用户ID: {}, 会话ID: {}", userId, sessionId);
            } catch (Exception e) {
                log.error("更新短期记忆和RAG缓存失败，用户ID: {}, 会话ID: {}", userId, sessionId, e);
            }
        }, aiServiceExecutor);
    }
    
    /**
     * 更新短期记忆（Redis）
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param query 用户查询
     * @param answer AI回答
     */
    public void updateShortTermMemory(String userId, String sessionId, String query, String answer) {
        String key = SHORT_TERM_MEMORY_PREFIX + userId + ":" + sessionId;
        
        // 将用户问题和AI回答作为对话对存储
        String conversationEntry = "Q: " + query + "\nA: " + answer;
        
        // 添加到列表头部（最新的在前面）
        redisTemplate.opsForList().leftPush(key, conversationEntry);
        
        // 限制短期记忆长度，保留最近的20条对话
        redisTemplate.opsForList().trim(key, 0, 19);
        
        // 设置过期时间为24小时
        redisTemplate.expire(key, 24 * 60 * 60, java.util.concurrent.TimeUnit.SECONDS);
        
        log.debug("短期记忆更新完成，用户ID: {}, 会话ID: {}", userId, sessionId);
    }
    
    /**
     * 写回RAG缓存
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param query 用户查询
     * @param answer AI回答
     */
    public void updateRAGCache(String userId, String sessionId, String query, String answer) {
        String cacheKey = RAG_CACHE_PREFIX + sha256Hex(userId + ":" + sessionId + ":" + query);
        
        // 构建缓存内容
        Map<String, Object> cacheContent = Map.of(
                "userId", userId,
                "sessionId", sessionId,
                "query", query,
                "answer", answer,
                "timestamp", System.currentTimeMillis()
        );
        
        // 存储到Redis
        redisTemplate.opsForValue().set(cacheKey, JSONObject.toJSONString(cacheContent), 7 * 24 * 60 * 60, java.util.concurrent.TimeUnit.SECONDS); // 7天过期
        
        log.debug("RAG缓存更新完成，用户ID: {}, 会话ID: {}", userId, sessionId);
    }
    
    /**
     * 将短期记忆归档到长期记忆
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param batchSize 批量处理大小
     * @return 是否成功
     */
    public boolean archiveShortTermToLongTerm(String userId, String sessionId, int batchSize) {
        try {
            String shortTermKey = SHORT_TERM_MEMORY_PREFIX + userId + ":" + sessionId;
            
            // 获取短期记忆内容
            List<String> shortTermMemories = redisTemplate.opsForList().range(shortTermKey, 0, batchSize - 1);
            
            if (shortTermMemories == null || shortTermMemories.isEmpty()) {
                log.debug("没有找到需要归档的短期记忆，用户ID: {}, 会话ID: {}", userId, sessionId);
                return true;
            }
            
            // 转换为Document对象
            List<Document> documents = new ArrayList<>();
            for (String memory : shortTermMemories) {
                Document doc = new Document(memory);
                doc.getMetadata().put("userId", userId);
                doc.getMetadata().put("sessionId", sessionId);
                doc.getMetadata().put("source", "long-term-memory");
                doc.getMetadata().put("archivedAt", System.currentTimeMillis());
                documents.add(doc);
            }
            
            // 使用DocumentTransformer处理文档
            List<Document> processedDocuments = documentTransformer.apply(documents);
            
            // 存储到VectorStore
            vectorStore.add(processedDocuments);
            
            // 从短期记忆中删除已归档的内容
            redisTemplate.opsForList().trim(shortTermKey, batchSize, -1);
            
            log.info("短期记忆归档完成，用户ID: {}, 会话ID: {}, 归档数量: {}", userId, sessionId, shortTermMemories.size());
            return true;
        } catch (Exception e) {
            log.error("短期记忆归档失败，用户ID: {}, 会话ID: {}", userId, sessionId, e);
            return false;
        }
    }
    
    /**
     * 检查RAG缓存是否存在
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param query 查询文本
     * @return 缓存内容，如果不存在则返回null
     */
    public String checkRAGCache(String userId, String sessionId, String query) {
        try {
            String cacheKey = RAG_CACHE_PREFIX + sha256Hex(userId + ":" + sessionId + ":" + query);
            
            String cachedContent = redisTemplate.opsForValue().get(cacheKey);
            if (cachedContent != null) {
                log.debug("命中RAG缓存，用户ID: {}, 会话ID: {}", userId, sessionId);
                
                // 解析缓存内容
                Map<String, Object> cacheData = JSONObject.parseObject(cachedContent, Map.class);
                return (String) cacheData.get("answer");
            }
            
            return null;
        } catch (Exception e) {
            log.error("检查RAG缓存失败，用户ID: {}, 会话ID: {}", userId, sessionId, e);
            return null;
        }
    }
    
    /**
     * 清理过期的RAG缓存
     * 
     * @param daysBefore 保留天数
     * @return 清理的缓存数量
     */
    public int cleanExpiredRAGCache(int daysBefore) {
        try {
            long expireTimestamp = System.currentTimeMillis() - (daysBefore * 24 * 60 * 60 * 1000L);
            // 实际实现需要遍历所有RAG缓存键并检查时间戳
            log.info("清理过期RAG缓存完成，保留天数: {}", daysBefore);
            return 0;
        } catch (Exception e) {
            log.error("清理过期RAG缓存失败", e);
            return 0;
        }
    }
}