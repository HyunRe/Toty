package com.toty.springconfig.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        int core = Runtime.getRuntime().availableProcessors() / 2; // CPU 코어 개수의 절반
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(core);       // 기본 스레드 수 (CPU 개수)
        executor.setMaxPoolSize(core * 2);    // 최대 스레드 수 (CPU 개수 * 2)
        executor.setQueueCapacity(500);       // 대기열 크기
        executor.setThreadNamePrefix("Async-Notification-");
        executor.initialize();
        return executor;
    }
}
