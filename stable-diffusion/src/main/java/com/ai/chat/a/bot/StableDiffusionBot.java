package com.ai.chat.a.bot;

import com.ai.chat.a.bot.BotRequest;
import com.ai.chat.a.bot.BotResponse;
import com.ai.chat.a.bot.BotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Stable Diffusion(comfyui)图片生成机器人插件
 */
@Component
@Slf4j
public class StableDiffusionBot implements BotService {
    
    @Override
    public String getName() {
        return "StableDiffusion";
    }

    @Override
    public String getDescription() {
        return "基于Stable Diffusion模型的AI图片生成服务";
    }
    
    @Override
    public String getType() {
        return "image";
    }
    
    @Override
    public boolean supports(BotRequest request) {
        // 检查请求中是否包含图片生成相关的参数
        return request != null && 
               (request.getMessageType() != null && request.getMessageType().equals("image") ||
                request.getParameter("image", false) ||
                request.getMessage() != null && 
                (request.getMessage().toLowerCase().contains("生成图片") || 
                 request.getMessage().toLowerCase().contains("generate image")));
    }

    @Override
    public BotResponse handleMessage(BotRequest request) {
        try {
            log.info("处理Stable Diffusion图片生成请求: {}", request.getMessage());
            
            // 从请求中提取图片生成参数
            String prompt = request.getMessage();
            Map<String, Object> parameters = request.getParameters();
            
            // 设置默认参数
            int width = getIntParam(parameters, "width", 512);
            int height = getIntParam(parameters, "height", 512);
            int steps = getIntParam(parameters, "steps", 20);
            double guidanceScale = getDoubleParam(parameters, "guidance_scale", 7.5);
            String negativePrompt = getStringParam(parameters, "negative_prompt", "");
            int seed = getIntParam(parameters, "seed", -1);
            
            // 构建图片生成请求
            Map<String, Object> imageParams = new HashMap<>();
            imageParams.put("prompt", prompt);
            imageParams.put("width", width);
            imageParams.put("height", height);
            imageParams.put("steps", steps);
            imageParams.put("guidance_scale", guidanceScale);
            imageParams.put("negative_prompt", negativePrompt);
            imageParams.put("seed", seed);
            imageParams.put("user_id", request.getUserId());
            imageParams.put("session_id", request.getSessionId());
            
            // 这里应该调用实际的Stable Diffusion API
            // 由于是示例，我们模拟一个图片URL
            String imageUrl = simulateImageGeneration(imageParams);
            
            // 构建响应
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("image_url", imageUrl);
            responseData.put("prompt", prompt);
            responseData.put("parameters", imageParams);
            
            return BotResponse.success("图片生成成功", responseData);
            
        } catch (Exception e) {
            log.error("处理Stable Diffusion图片生成请求失败", e);
            return BotResponse.failure("图片生成失败: " + e.getMessage());
        }
    }

    private String simulateImageGeneration(Map<String, Object> params) {
        log.info("图片生成，参数: {}", params);
        return "http://localhost:8122/generated-images/" + System.currentTimeMillis() + ".png";
    }
    
    private int getIntParam(Map<String, Object> params, String key, int defaultValue) {
        if (params == null || !params.containsKey(key)) {
            return defaultValue;
        }
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private double getDoubleParam(Map<String, Object> params, String key, double defaultValue) {
        if (params == null || !params.containsKey(key)) {
            return defaultValue;
        }
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private String getStringParam(Map<String, Object> params, String key, String defaultValue) {
        if (params == null || !params.containsKey(key)) {
            return defaultValue;
        }
        return String.valueOf(params.get(key));
    }
}