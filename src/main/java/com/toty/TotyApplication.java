package com.toty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableJpaAuditing // JPA Auditing 활성화 (createdAt, updatedAt 자동 생성)
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class}) // Spring Security 비활성화
@EnableScheduling // 매월 1일 멘토 선발 로직 수행 스케줄러
public class TotyApplication {
    public static void main(String[] args) {
        SpringApplication.run(TotyApplication.class, args);
    }
}