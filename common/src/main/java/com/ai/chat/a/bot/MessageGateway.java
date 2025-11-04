package com.ai.chat.a.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 消息网关
 * 负责消息路由和分发
 */
@Slf4j
@Service
public class MessageGateway {
    
    @Autowired
    private BotRegistry botRegistry;
    
    /**
     * 路由消息到指定的机器人
     * @param request 消息请求
     * @return 机器人响应
     */
    public BotResponse routeMessage(BotRequest request) {
        if (request == null) {
            return BotResponse.failure("请求不能为空");
        }
        
        String targetBot = request.getTargetBot();
        if (targetBot == null || targetBot.trim().isEmpty()) {
            return BotResponse.failure("未指定目标机器人");
        }
        
        BotService bot = botRegistry.getBot(targetBot);
        if (bot == null) {
            log.warn("未找到机器人: {}", targetBot);
            return BotResponse.failure("未找到指定的机器人: " + targetBot);
        }
        
        try {
            log.info("路由消息到机器人: {}", targetBot);
            BotResponse response = bot.handleMessage(request);
            log.info("机器人响应: success={}, message={}", response.getSuccess(), response.getMessage());
            return response;
        } catch (Exception e) {
            log.error("处理消息时发生错误", e);
            return BotResponse.failure("BOT_ERROR", "机器人处理消息时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据消息内容自动选择合适的机器人
     * @param request 消息请求
     * @return 机器人响应
     */
    public BotResponse autoRouteMessage(BotRequest request) {
        if (request == null) {
            return BotResponse.failure("请求不能为空");
        }
        
        Map<String, BotService> allBots = botRegistry.getAllBots();
        if (allBots.isEmpty()) {
            return BotResponse.failure("没有可用的机器人");
        }
        
        // 尝试找到支持该请求的机器人
        for (BotService bot : allBots.values()) {
            try {
                if (bot.supports(request)) {
                    log.info("自动路由消息到机器人: {}", bot.getName());
                    return bot.handleMessage(request);
                }
            } catch (Exception e) {
                log.warn("机器人 {} 处理消息时出错，尝试下一个机器人", bot.getName(), e);
            }
        }
        
        // 如果没有机器人支持，返回默认机器人或错误
        return BotResponse.failure("没有找到能够处理该请求的机器人");
    }
    
    /**
     * 根据类型获取机器人列表
     * @param type 机器人类型
     * @return 机器人列表
     */
    public List<BotService> getBotsByType(String type) {
        return botRegistry.getBotsByType(type);
    }
    
    /**
     * 获取所有已注册的机器人信息
     * @return 机器人信息列表
     */
    public Map<String, String> getAllBotInfo() {
        Map<String, BotService> allBots = botRegistry.getAllBots();
        Map<String, String> botInfo = new java.util.HashMap<>();
        
        allBots.forEach((name, bot) -> {
            botInfo.put(name, bot.getDescription());
        });
        
        return botInfo;
    }
}