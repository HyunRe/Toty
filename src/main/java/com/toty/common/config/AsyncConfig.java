package com.toty.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 처리 설정
 * - 이미지 삭제 등 시간이 걸리는 작업을 비동기로 처리
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 비동기 작업을 위한 Thread Pool 설정
     * - Core Pool Size: 2 (기본 스레드 수)
     * - Max Pool Size: 5 (최대 스레드 수)
     * - Queue Capacity: 100 (대기 큐 크기)
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();

        log.info("비동기 작업 Thread Pool 초기화 완료 - Core: {}, Max: {}, Queue: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
}
