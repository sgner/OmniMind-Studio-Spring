package com.ai.chat.a.bot;

import com.ai.chat.a.bot.BotRequest;
import com.ai.chat.a.bot.BotResponse;
import com.ai.chat.a.bot.BotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * XF角色扮演机器人插件
 */
@Component
@Slf4j
public class XfRolePlayBot implements BotService {
    
    @Override
    public String getName() {
        return "XfRolePlay";
    }

    @Override
    public String getDescription() {
        return "基于讯飞星火的角色扮演AI服务";
    }
    
    @Override
    public String getType() {
        return "roleplay";
    }
    
    @Override
    public boolean supports(BotRequest request) {
        // 支持角色扮演相关关键词
        return request != null && 
               (request.getParameter("roleplay", false) ||
                request.getMessage() != null && 
                (request.getMessage().toLowerCase().contains("角色扮演") || 
                 request.getMessage().toLowerCase().contains("role play")));
    }

    @Override
    public BotResponse handleMessage(BotRequest request) {
        try {
            log.info("处理XF角色扮演请求: {}", request.getMessage());
            
            // 从请求中提取角色扮演参数
            String message = request.getMessage();
            Map<String, Object> parameters = request.getParameters();
            
            // 获取角色ID和用户ID
            String roleId = getStringParam(parameters, "role_id", "");
            String userId = request.getUserId();
            String sessionId = request.getSessionId();
            
            // 获取上下文信息
            String context = getStringParam(parameters, "context", "");
            
            // 构建角色扮演请求
            Map<String, Object> rolePlayParams = new HashMap<>();
            rolePlayParams.put("message", message);
            rolePlayParams.put("role_id", roleId);
            rolePlayParams.put("user_id", userId);
            rolePlayParams.put("session_id", sessionId);
            rolePlayParams.put("context", context);
            
            // 这里应该调用实际的XF角色扮演API
            // 由于是示例，我们模拟一个角色扮演响应
            String response = simulateRolePlay(rolePlayParams);
            
            // 构建响应
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("response", response);
            responseData.put("role_id", roleId);
            responseData.put("user_id", userId);
            responseData.put("session_id", sessionId);
            
            return BotResponse.success("角色扮演响应成功", responseData);
            
        } catch (Exception e) {
            log.error("处理XF角色扮演请求失败", e);
            return BotResponse.failure("角色扮演失败: " + e.getMessage());
        }
    }
    
    /**
     * 模拟角色扮演过程
     */
    private String simulateRolePlay(Map<String, Object> params) {
        log.info("模拟角色扮演，参数: {}", params);
        // 这里应该调用实际的XF角色扮演API
        // 返回一个模拟的角色扮演响应
        String roleId = (String) params.get("role_id");
        String message = (String) params.get("message");
        
        // 根据角色ID和消息内容生成模拟响应
        if (roleId != null && !roleId.isEmpty()) {
            return "【角色" + roleId + "】根据您的消息\"" + message + "\"，我作为角色回应：这是一个模拟的角色扮演响应。";
        } else {
            return "根据您的消息\"" + message + "\"，我回应：这是一个模拟的AI响应。";
        }
    }
    
    private String getStringParam(Map<String, Object> params, String key, String defaultValue) {
        if (params == null || !params.containsKey(key)) {
            return defaultValue;
        }
        return String.valueOf(params.get(key));
    }
}