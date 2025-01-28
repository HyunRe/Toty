package com.toty.redisConfig;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis 단일 데이터를 처리하는 비즈니스 로직 인터페이스입니다.
 */
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisHandler redisHandler;
    private final RedisConfig redisConfig;

    int setData(String key, Object value) {
        return redisHandler.executeOperation(() -> redisHandler.getValueOperations().set(key, value));
    }

    int setData(String key, Object value, Duration duration) {
        return redisHandler.executeOperation(() -> redisHandler.getValueOperations().set(key, value, duration));
    }

    String getData(String key) {
        if (redisHandler.getValueOperations().get(key) == null) return "";
        return String.valueOf(redisHandler.getValueOperations().get(key));
    }

    int deleteData(String key) {
        return redisHandler.executeOperation(() -> redisConfig.redisTemplate().delete(key));
    }
}