package com.ai.chat.a.bot;

import com.ai.chat.a.bot.BotRequest;
import com.ai.chat.a.bot.BotResponse;
import com.ai.chat.a.bot.BotService;
import com.ai.chat.a.mq.GenVideoSender;
import com.ai.chat.a.properties.LumaProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Luma视频生成机器人插件
 */
@Slf4j
@Service("luma")
public class LumaVideoBot implements BotService {
    
    @Autowired
    private LumaProperties lumaProperties;
    
    @Autowired
    private GenVideoSender genVideoSender;
    
    @Override
    public String getName() {
        return "luma";
    }
    
    @Override
    public String getDescription() {
        return "Luma AI视频生成服务，可以根据文本描述生成视频";
    }
    
    @Override
    public String getType() {
        return "video";
    }
    
    @Override
    public boolean supports(BotRequest request) {
        // 检查请求中是否包含视频生成相关的参数
        return request != null && 
               (request.getMessageType() != null && request.getMessageType().equals("video") ||
                request.getParameter("video", false) ||
                request.getMessage() != null && 
                (request.getMessage().toLowerCase().contains("生成视频") || 
                 request.getMessage().toLowerCase().contains("generate video")));
    }
    
    @Override
    public BotResponse handleMessage(BotRequest request) {
        try {
            log.info("Luma视频生成机器人处理请求: {}", request.getMessage());
            
            // 获取请求参数
            String prompt = request.getMessage();
            String userId = request.getUserId();
            String sessionId = request.getSessionId();
            
            // 生成任务ID
            String taskId = UUID.randomUUID().toString();
            
            // 构建视频生成参数
            Map<String, Object> videoParams = new HashMap<>();
            videoParams.put("prompt", prompt);
            videoParams.put("userId", userId);
            videoParams.put("sessionId", sessionId);
            videoParams.put("taskId", taskId);
            
            // 获取其他可选参数
            if (request.getParameter("duration",null) != null) {
                videoParams.put("duration", request.getParameter("duration",null));
            }
            if (request.getParameter("aspectRatio",null) != null) {
                videoParams.put("aspectRatio", request.getParameter("aspectRatio",null));
            }
            if (request.getParameter("loop",null) != null) {
                videoParams.put("loop", request.getParameter("loop",null));
            }
            
            // 发送视频生成请求到消息队列
            genVideoSender.sendMessageForCommit(videoParams);
            
            // 构建响应
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("taskId", taskId);
            responseData.put("status", "processing");
            responseData.put("message", "视频生成任务已提交，请稍后查询结果");
            
            return BotResponse.success("视频生成任务已提交", responseData);
            
        } catch (Exception e) {
            log.error("Luma视频生成机器人处理请求时发生错误", e);
            return BotResponse.failure("LUMA_ERROR", "视频生成失败: " + e.getMessage());
        }
    }
}