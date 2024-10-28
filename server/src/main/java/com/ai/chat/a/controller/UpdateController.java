package com.ai.chat.a.controller;

import com.ai.chat.a.po.Session;
import com.ai.chat.a.result.R;
import com.ai.chat.a.service.SessionService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/update")
public class UpdateController {
    @Resource
    private SessionService sessionService;
   @GetMapping("/sessionNoReadCount")
    public R updateSessionNoReadCount(String sessionId) {
        sessionService.update(new LambdaUpdateWrapper<Session>().eq(Session::getSessionId,sessionId).set(Session::getNoReadCount,0));
        return R.success();
    }
}
