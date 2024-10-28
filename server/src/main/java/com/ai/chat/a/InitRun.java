package com.ai.chat.a;

import com.ai.chat.a.websocket.netty.NeeyWebSocketStarter;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitRun implements ApplicationRunner {
    @Resource
    private NeeyWebSocketStarter neeyWebSocketStarter;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        new Thread(neeyWebSocketStarter).start();
    }
}
