package com.ai.chat.a.banner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


@Configuration
@Slf4j
public class SunoBanner {
    @PostConstruct
    public void init() {
        System.out.println("" +
                "███████╗██╗   ██╗███╗   ██╗ ██████╗        █████╗ ██╗\n" +
                "██╔════╝██║   ██║████╗  ██║██╔═══██╗      ██╔══██╗██║\n" +
                "███████╗██║   ██║██╔██╗ ██║██║   ██║█████╗███████║██║\n" +
                "╚════██║██║   ██║██║╚██╗██║██║   ██║╚════╝██╔══██║██║\n" +
                "███████║╚██████╔╝██║ ╚████║╚██████╔╝      ██║  ██║██║\n" +
                "╚══════╝ ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝       ╚═╝  ╚═╝╚═╝\n" +
                "                                                     ");
        System.out.println("::SUNO-AI::                                           (0.0.1)");

    }
}
