package com.ai.chat.a.rag;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Embedding缓存服务
 * 实现文本归一化、hash功能和缓存管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingCacheService {

    private final StringRedisTemplate redisTemplate;
    private final EmbeddingModel embeddingModel;
    
    // 缓存键前缀
    private static final String EMBEDDING_CACHE_PREFIX = "embedding:cache:";
    // 缓存过期时间（7天）
    private static final long CACHE_EXPIRE_HOURS = 168;
    
    /**
     * 获取文本的Embedding，优先从缓存中获取
     * 
     * @param text 输入文本
     * @return 文本的Embedding向量
     */
    public float[] getEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[0];
        }
        
        // 文本归一化
        String normalizedText = normalizeText(text);
        
        // 计算文本hash
        String textHash = calculateHash(normalizedText);
        String cacheKey = EMBEDDING_CACHE_PREFIX + textHash;
        
        // 尝试从Redis缓存获取
        String cachedEmbeddingStr = redisTemplate.opsForValue().get(cacheKey);
        if (cachedEmbeddingStr != null) {
            try {
                float[] cachedEmbedding = JSON.parseObject(cachedEmbeddingStr, float[].class);
                log.debug("从缓存中获取Embedding，文本hash: {}", textHash);
                return cachedEmbedding;
            } catch (Exception e) {
                log.warn("解析缓存的Embedding失败，文本hash: {}", textHash, e);
            }
        }
        
        // 缓存未命中，生成新的Embedding
        log.debug("缓存未命中，生成新的Embedding，文本hash: {}", textHash);
        float[] embedding = generateEmbedding(normalizedText);
        
        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(embedding), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        
        return embedding;
    }
    
    /**
     * 批量获取文本的Embedding
     * 
     * @param texts 文本列表
     * @return Embedding向量列表
     */
    public List<float[]> getEmbeddings(List<String> texts) {
        return texts.parallelStream()
                .map(this::getEmbedding)
                .toList();
    }
    
    /**
     * 获取文档的Embedding
     * 
     * @param document 文档对象
     * @return 文档的Embedding向量
     */
    public float[] getDocumentEmbedding(Document document) {
        if (document == null || document.getContent() == null) {
            return new float[0];
        }
        return getEmbedding(document.getContent());
    }
    
    /**
     * 文本归一化处理
     * 
     * @param text 原始文本
     * @return 归一化后的文本
     */
    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        
        // 去除首尾空白
        text = text.trim();
        
        // Unicode规范化（NFKC）
        text = Normalizer.normalize(text, Normalizer.Form.NFKC);
        
        // 转换为小写
        text = text.toLowerCase();
        
        // 移除多余的空白字符
        text = text.replaceAll("\\s+", " ");
        
        return text;
    }
    
    /**
     * 计算文本的SHA-256哈希值
     * 
     * @param text 输入文本
     * @return 文本的哈希值
     */
    private String calculateHash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(text.getBytes());
            
            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("计算文本哈希失败", e);
            // 降级处理：使用文本的hashCode
            return String.valueOf(text.hashCode());
        }
    }
    
    /**
     * 生成文本的Embedding向量
     * 
     * @param text 输入文本
     * @return Embedding向量
     */
    private float[] generateEmbedding(String text) {
        try {
            EmbeddingRequest request = new EmbeddingRequest(List.of(text), null);
            EmbeddingResponse response = embeddingModel.call(request);
            
            if (response.getResults() != null && !response.getResults().isEmpty()) {
                return response.getResults().get(0).getOutput();
            }
        } catch (Exception e) {
            log.error("生成Embedding失败", e);
        }
        
        // 如果生成失败，返回空数组
        return new float[0];
    }
    
    /**
     * 清除指定文本的缓存
     * 
     * @param text 要清除缓存的文本
     */
    public void clearCache(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        
        String normalizedText = normalizeText(text);
        String textHash = calculateHash(normalizedText);
        String cacheKey = EMBEDDING_CACHE_PREFIX + textHash;
        
        redisTemplate.delete(cacheKey);
        log.info("已清除文本缓存，hash: {}", textHash);
    }
    
    /**
     * 清除所有Embedding缓存
     */
    public void clearAllCache() {
        // 这里需要根据RedisUtil的实现来删除匹配前缀的所有键
        // 假设RedisUtil提供了批量删除方法
        log.info("清除所有Embedding缓存");
        // 实际实现可能需要使用Redis的SCAN命令或KEYS命令
        // redisUtil.deleteByPattern(EMBEDDING_CACHE_PREFIX + "*");
    }
}