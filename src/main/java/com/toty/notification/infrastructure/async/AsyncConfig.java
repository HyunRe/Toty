package com.toty.notification.infrastructure.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    /**
     * 알림 전송을 위한 비동기 작업을 처리하는 Executor를 정의합니다.
     * @Bean의 name 속성은 @Async 어노테이션에서 참조됩니다.
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 기본 스레드 수
        executor.setMaxPoolSize(10); // 최대 스레드 수
        executor.setQueueCapacity(25); // 큐에 대기할 수 있는 작업 수
        executor.setThreadNamePrefix("Notification-Async-"); // 스레드 이름 접두사
        executor.initialize(); // Executor 초기화
        return executor;
    }
}
