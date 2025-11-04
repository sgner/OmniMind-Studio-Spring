package com.ai.chat.a.bot;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 统一的机器人响应模型
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BotResponse {
    
    /**
     * 响应是否成功
     */
    private Boolean success;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private Object data;
    
    /**
     * 响应类型（text, image, audio, video等）
     */
    private String responseType;
    
    /**
     * 错误码（失败时使用）
     */
    private String errorCode;
    
    /**
     * 错误信息（失败时使用）
     */
    private String errorMessage;
    
    /**
     * 附加元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 响应时间戳
     */
    private Long timestamp;
    
    /**
     * 创建成功响应
     * @param message 响应消息
     * @return 成功响应对象
     */
    public static BotResponse success(String message) {
        return BotResponse.builder()
                .success(true)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建成功响应（带数据）
     * @param message 响应消息
     * @param data 响应数据
     * @return 成功响应对象
     */
    public static BotResponse success(String message, Object data) {
        return BotResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建失败响应
     * @param message 响应消息
     * @return 失败响应对象
     */
    public static BotResponse failure(String message) {
        return BotResponse.builder()
                .success(false)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建失败响应（带错误码）
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     * @return 失败响应对象
     */
    public static BotResponse failure(String errorCode, String errorMessage) {
        return BotResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 设置元数据
     * @param key 元数据键
     * @param value 元数据值
     */
    public void setMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }
}