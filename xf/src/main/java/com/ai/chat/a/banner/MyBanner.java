package com.ai.chat.a.banner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Slf4j
public class MyBanner {
    @PostConstruct
    public void init() {
        System.out.println("                                                   \n" +
                "                          )    (                )  \n" +
                "               )  (    ( /(    )\\ )   (      ( /(  \n" +
                " (   `  )   ( /(  )(   )\\())  (()/(  ))\\ (   )\\()) \n" +
                " )\\  /(/(   )(_))(()\\ ((_)\\    ((_))/((_))\\ ((_)\\  \n" +
                "((_)((_)_\\ ((_)_  ((_)| |(_)   _| |(_)) ((_)| |(_) \n" +
                "(_-<| '_ \\)/ _` || '_|| / /  / _` |/ -_)(_-<| / /  \n" +
                "/__/| .__/ \\__,_||_|  |_\\_\\  \\__,_|\\___|/__/|_\\_\\  \n" +
                "    |_|                                            ");
        System.out.println("::星火大模型::                                           (0.0.1)");

    }
}
