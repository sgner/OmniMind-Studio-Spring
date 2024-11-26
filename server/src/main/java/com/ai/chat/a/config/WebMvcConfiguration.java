package com.ai.chat.a.config;

import com.ai.chat.a.interceptor.JwtTokenUserInterceptor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
@Slf4j
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {
   @Resource
   private JwtTokenUserInterceptor jwtTokenUserInterceptor;
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
//        registry.addInterceptor(jwtTokenAdminInterceptor)
//                .addPathPatterns("/admin/**")
//                .excludePathPatterns("/admin/employee/login");
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .addPathPatterns("/chat/**")
                .addPathPatterns("/robot/**")
                .addPathPatterns("/upload/**")
                .addPathPatterns("/suno/**")
                .addPathPatterns("/xf/**")
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/user/register");
    }
}
