package com.ai.chat.a.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 长期记忆归档定时任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LongTermMemoryArchiveService {

    private final MemoryUpdateService memoryUpdateService;
    private final StringRedisTemplate redisTemplate;
    private final RedisConnectionFactory redisConnectionFactory;
    
    // Redis键前缀
    private static final String SHORT_TERM_MEMORY_PREFIX = "memory:short:";
    private static final String USER_SESSIONS_PREFIX = "user:sessions:";
    
    /**
     * 每天凌晨2点执行长期记忆归档任务
     * 将所有用户的短期记忆归档到长期记忆
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void archiveLongTermMemory() {
        log.info("开始执行长期记忆归档任务");
        
        try {
            // 获取所有用户会话
            Set<String> userSessionKeys = scanKeys(USER_SESSIONS_PREFIX + "*");
            
            int totalArchived = 0;
            int failedCount = 0;
            
            for (String userSessionKey : userSessionKeys) {
                try {
                    // 从键中提取用户ID和会话ID
                    String keySuffix = userSessionKey.substring(USER_SESSIONS_PREFIX.length());
                    String[] parts = keySuffix.split(":", 2);
                    
                    if (parts.length < 2) {
                        log.warn("无法解析用户会话键: {}", userSessionKey);
                        continue;
                    }
                    
                    String userId = parts[0];
                    String sessionId = parts[1];
                    
                    // 归档该会话的短期记忆
                    boolean success = memoryUpdateService.archiveShortTermToLongTerm(userId, sessionId, 50);
                    
                    if (success) {
                        totalArchived++;
                    } else {
                        failedCount++;
                    }
                } catch (Exception e) {
                    log.error("归档用户会话失败: {}", userSessionKey, e);
                    failedCount++;
                }
            }
            
            log.info("长期记忆归档任务完成，成功归档: {}, 失败: {}", totalArchived, failedCount);
            
            // 清理过期的RAG缓存
            cleanExpiredRAGCache();
            
        } catch (Exception e) {
            log.error("长期记忆归档任务执行失败", e);
        }
    }
    
    /**
     * 每小时执行一次，检查并归档活跃会话的短期记忆
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void archiveActiveSessions() {
        log.debug("开始执行活跃会话归档任务");
        
        try {
            // 获取所有短期记忆键
            Set<String> shortTermKeys = scanKeys(SHORT_TERM_MEMORY_PREFIX + "*");
            
            int totalArchived = 0;
            
            for (String shortTermKey : shortTermKeys) {
                try {
                    // 检查短期记忆长度，如果超过阈值则归档
                    Long length = redisTemplate.opsForList().size(shortTermKey);
                    
                    if (length != null && length > 30) { // 超过30条对话则归档
                        // 从键中提取用户ID和会话ID
                        String keySuffix = shortTermKey.substring(SHORT_TERM_MEMORY_PREFIX.length());
                        String[] parts = keySuffix.split(":", 2);
                        
                        if (parts.length < 2) {
                            log.warn("无法解析短期记忆键: {}", shortTermKey);
                            continue;
                        }
                        
                        String userId = parts[0];
                        String sessionId = parts[1];
                        
                        // 归档该会话的短期记忆
                        boolean success = memoryUpdateService.archiveShortTermToLongTerm(userId, sessionId, 20);
                        
                        if (success) {
                            totalArchived++;
                        }
                    }
                } catch (Exception e) {
                    log.error("归档活跃会话失败: {}", shortTermKey, e);
                }
            }
            
            if (totalArchived > 0) {
                log.info("活跃会话归档任务完成，归档数量: {}", totalArchived);
            }
            
        } catch (Exception e) {
            log.error("活跃会话归档任务执行失败", e);
        }
    }
    
    /**
     * 每周执行一次，清理过期的RAG缓存
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void cleanExpiredRAGCache() {
        log.info("开始清理过期的RAG缓存");
        
        try {
            // 清理7天前的RAG缓存
            int cleanedCount = memoryUpdateService.cleanExpiredRAGCache(7);
            
            log.info("RAG缓存清理完成，清理数量: {}", cleanedCount);
            
        } catch (Exception e) {
            log.error("RAG缓存清理失败", e);
        }
    }
    
    /**
     * 每天执行一次，清理过期的短期记忆
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void cleanExpiredShortTermMemory() {
        log.info("开始清理过期的短期记忆");
        
        try {
            // 获取所有短期记忆键
            Set<String> shortTermKeys = scanKeys(SHORT_TERM_MEMORY_PREFIX + "*");
            
            int cleanedCount = 0;
            
            for (String shortTermKey : shortTermKeys) {
                try {
                    // 检查键的TTL
                    Long ttl = redisTemplate.getExpire(shortTermKey, TimeUnit.SECONDS);
                    
                    // 如果TTL小于等于0或者即将过期（剩余时间小于1小时），则删除
                    if (ttl != null && (ttl <= 0 || ttl < 3600)) {
                        redisTemplate.delete(shortTermKey);
                        cleanedCount++;
                    }
                } catch (Exception e) {
                    log.error("清理短期记忆失败: {}", shortTermKey, e);
                }
            }
            
            log.info("短期记忆清理完成，清理数量: {}", cleanedCount);
            
        } catch (Exception e) {
            log.error("短期记忆清理任务执行失败", e);
        }
    }
    
    /**
     * 扫描Redis键
     * 
     * @param pattern 键模式
     * @return 匹配的键集合
     */
    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        RedisConnection connection = null;
        try {
            connection = redisConnectionFactory.getConnection();
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(1000).build());
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
        } catch (Exception e) {
            log.error("扫描Redis键失败: {}", pattern, e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return keys;
    }
}