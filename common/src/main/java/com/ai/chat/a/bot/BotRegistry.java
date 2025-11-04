package com.ai.chat.a.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 机器人注册中心
 * 负责管理所有已注册的机器人服务
 */
@Slf4j
@Component
public class BotRegistry {
    
    /**
     * 机器人服务映射表，key为机器人名称
     */
    private final Map<String, BotService> bots = new ConcurrentHashMap<>();
    
    /**
     * 按类型分组的机器人服务映射表，key为机器人类型
     */
    private final Map<String, List<BotService>> botsByType = new ConcurrentHashMap<>();
    
    /**
     * 构造函数，自动注入所有BotService实现
     * @param services 所有BotService实现类
     */
    @Autowired
    public BotRegistry(List<BotService> services) {
        log.info("开始注册机器人服务...");
        for (BotService bot : services) {
            registerBot(bot);
        }
        log.info("机器人服务注册完成，共注册{}个机器人", bots.size());
    }
    
    /**
     * 注册机器人服务
     * @param bot 机器人服务
     */
    private void registerBot(BotService bot) {
        String name = bot.getName();
        if (name == null || name.trim().isEmpty()) {
            log.warn("机器人服务名称为空，跳过注册: {}", bot.getClass().getName());
            return;
        }
        
        if (bots.containsKey(name)) {
            log.warn("机器人服务名称已存在，将被覆盖: {}", name);
        }
        
        bots.put(name, bot);
        
        // 按类型分组
        String type = bot.getType();
        botsByType.computeIfAbsent(type, k -> new java.util.ArrayList<>()).add(bot);
        
        log.info("注册机器人服务: {} ({})", name, bot.getDescription());
    }
    
    /**
     * 根据名称获取机器人服务
     * @param name 机器人名称
     * @return 机器人服务，如果不存在则返回null
     */
    public BotService getBot(String name) {
        return bots.get(name);
    }
    
    /**
     * 根据类型获取机器人服务列表
     * @param type 机器人类型
     * @return 机器人服务列表
     */
    public List<BotService> getBotsByType(String type) {
        return botsByType.getOrDefault(type, java.util.Collections.emptyList());
    }
    
    /**
     * 获取所有已注册的机器人服务
     * @return 机器人服务映射表
     */
    public Map<String, BotService> getAllBots() {
        return new ConcurrentHashMap<>(bots);
    }
    
    /**
     * 检查机器人是否已注册
     * @param name 机器人名称
     * @return 是否已注册
     */
    public boolean isBotRegistered(String name) {
        return bots.containsKey(name);
    }
    
    /**
     * 获取已注册机器人的数量
     * @return 机器人数量
     */
    public int getBotCount() {
        return bots.size();
    }
    
    /**
     * 获取所有机器人类型
     * @return 机器人类型集合
     */
    public java.util.Set<String> getBotTypes() {
        return botsByType.keySet();
    }
}