package com.ai.chat.a;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class AChatAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AChatAiApplication.class, args);
    }
}
