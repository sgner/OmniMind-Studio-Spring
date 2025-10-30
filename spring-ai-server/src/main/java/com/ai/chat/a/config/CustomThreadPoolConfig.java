package com.ai.chat.a.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程池配置类
 * 用于AI服务请求的并发处理
 */
@Configuration
@Slf4j
public class CustomThreadPoolConfig {

    /**
     * 创建用于AI服务请求处理的线程池
     * 核心线程数：8，最大线程数：16
     */
    @Bean("aiServiceExecutor")
    public ThreadPoolExecutor aiServiceExecutor() {
        // 线程工厂，用于创建线程并设置线程名称
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "ai-service-thread-" + threadNumber.getAndIncrement());
                thread.setDaemon(false); // 非守护线程
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            }
        };

        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                8, // 核心线程数
                16, // 最大线程数
                60L, TimeUnit.SECONDS, // 非核心线程空闲超时时间
                new LinkedBlockingQueue<>(100), // 工作队列
                threadFactory, // 线程工厂
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：调用者执行
        ) {
            /**
             * 重写afterExecute方法，捕获并记录任务执行过程中的异常
             */
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                if (t != null) {
                    // 直接异常
                    log.error("AI任务执行异常", t);
                } else if (r instanceof Future<?>) {
                    // 检查Future中的异常
                    try {
                        Future<?> future = (Future<?>) r;
                        if (future.isDone()) {
                            future.get();
                        }
                    } catch (CancellationException ce) {
                        log.error("AI任务被取消", ce);
                    } catch (ExecutionException ee) {
                        log.error("AI任务执行异常", ee.getCause());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); // 恢复中断状态
                        log.error("AI任务被中断", ie);
                    }
                }
            }

            /**
             * 重写beforeExecute方法，记录任务开始执行
             */
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                super.beforeExecute(t, r);
                log.debug("AI任务开始执行，线程名称: {}", t.getName());
            }

            /**
             * 重写terminated方法，记录线程池终止
             */
            @Override
            protected void terminated() {
                super.terminated();
                log.info("AI服务线程池已终止");
            }
        };

        // 设置线程池参数
        executor.allowCoreThreadTimeOut(true); // 允许核心线程超时

        return executor;
    }
}