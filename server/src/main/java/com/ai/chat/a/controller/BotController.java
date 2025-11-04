package com.ai.chat.a.controller;

import com.ai.chat.a.bot.BotRequest;
import com.ai.chat.a.bot.BotResponse;
import com.ai.chat.a.bot.BotService;
import com.ai.chat.a.bot.MessageGateway;
import com.ai.chat.a.enums.ErrorCode;
import com.ai.chat.a.result.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统一的机器人控制器，使用消息网关处理所有AI服务请求
 */
@RestController
@RequestMapping("/bot")
@RequiredArgsConstructor
@Slf4j
public class BotController {
    
    private final MessageGateway messageGateway;
    
    /**
     * 自动路由消息到合适的机器人
     * @param request 请求参数
     * @return 响应结果
     */
    @PostMapping("/auto-route")
    public R autoRouteMessage(@RequestBody BotRequest request) {
        try {
            log.info("自动路由消息请求: {}", request);
            BotResponse response = messageGateway.autoRouteMessage(request);
            return R.success(response);
        } catch (Exception e) {
            log.error("自动路由消息失败", e);
            return R.error(ErrorCode.SYSTEM_ERROR,"自动路由消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 指定机器人处理消息
     * @param request 请求参数
     * @return 响应结果
     */
    @PostMapping("/route")
    public R routeMessage(@RequestBody BotRequest request) {
        try {
            log.info("指定机器人处理消息请求:  request={}", request);
            // 设置目标机器人名称
            if (request.getTargetBot() == null || request.getTargetBot().trim().isEmpty()) {
                return R.error(ErrorCode.SYSTEM_ERROR, "未指定目标机器人");
            }
            BotResponse response = messageGateway.routeMessage(request);
            return R.success(response);
        } catch (Exception e) {
            log.error("指定机器人处理消息失败", e);
            return R.error(ErrorCode.SYSTEM_ERROR,"指定机器人处理消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有机器人信息
     * @return 机器人列表
     */
    @GetMapping("/list")
    public R<Map<String, String>> getAllBotInfo() {
        try {
            log.info("获取所有机器人信息");
            Map<String, String> botInfo = messageGateway.getAllBotInfo();
            return R.success(botInfo);
        } catch (Exception e) {
            log.error("获取所有机器人信息失败", e);
            return R.error(ErrorCode.SYSTEM_ERROR,"获取所有机器人信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 按类型获取机器人
     *
     * @param type 机器人类型
     * @return 机器人列表
     */
    @GetMapping("/list/{type}")
    public R getBotsByType(@PathVariable String type) {
        try {
            log.info("按类型获取机器人: type={}", type);
            List<BotService> botInfo = messageGateway.getBotsByType(type);
            return R.success(botInfo);
        } catch (Exception e) {
            log.error("按类型获取机器人失败", e);
            return R.error(ErrorCode.SYSTEM_ERROR,"按类型获取机器人失败: " + e.getMessage());
        }
    }
}