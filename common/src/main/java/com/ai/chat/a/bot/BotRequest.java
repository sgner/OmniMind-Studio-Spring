package com.ai.chat.a.bot;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 统一的机器人请求模型
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BotRequest {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 消息内容
     */
    private String message;
    
    /**
     * 消息类型（text, image, audio等）
     */
    private String messageType;
    
    /**
     * 附加参数
     */
    private Map<String, Object> parameters;
    
    /**
     * 请求时间戳
     */
    private Long timestamp;
    
    /**
     * 目标机器人名称（用于路由）
     */
    private String targetBot;
    
    /**
     * 获取指定参数
     * @param key 参数键
     * @param defaultValue 默认值
     * @param <T> 参数类型
     * @return 参数值
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, T defaultValue) {
        if (parameters == null || !parameters.containsKey(key)) {
            return defaultValue;
        }
        return (T) parameters.get(key);
    }
    
    /**
     * 设置参数
     * @param key 参数键
     * @param value 参数值
     */
    public void setParameter(String key, Object value) {
        if (parameters == null) {
            parameters = new java.util.HashMap<>();
        }
        parameters.put(key, value);
    }
}