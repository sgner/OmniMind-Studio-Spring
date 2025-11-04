package com.ai.chat.a.bot;

/**
 * AI机器人服务统一接口
 * 所有AI服务插件都需要实现此接口
 */
public interface BotService {
    
    /**
     * 获取机器人名称，用于标识和路由
     * @return 机器人唯一名称
     */
    String getName();
    
    /**
     * 获取机器人描述
     * @return 机器人功能描述
     */
    String getDescription();
    
    /**
     * 处理消息请求
     * @param request 统一的消息请求对象
     * @return 统一的消息响应对象
     */
    BotResponse handleMessage(BotRequest request);
    
    /**
     * 获取机器人类型
     * @return 机器人类型（如：chat, image, video, voice等）
     */
    default String getType() {
        return "default";
    }
    
    /**
     * 检查是否支持该类型的请求
     * @param request 请求对象
     * @return 是否支持
     */
    default boolean supports(BotRequest request) {
        return true;
    }
}