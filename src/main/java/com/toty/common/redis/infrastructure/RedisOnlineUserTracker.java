package com.toty.common.redis.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisOnlineUserTracker {
    private final String KEY_PREFIX = "sse:user:";
    private final RedisTemplate<String, String> redisTemplate;

    public void markOnline(Long userId) {
        redisTemplate.opsForValue().set(KEY_PREFIX + userId, "online", Duration.ofMinutes(30));
    }

    public void markOffline(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }

    public boolean isOnline(Long userId) {
        return redisTemplate.hasKey(KEY_PREFIX + userId);
    }
}
