package com.toty.notification.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toty.base.exception.JsonProcessingCustomException;
import com.toty.notification.presentation.dto.request.NotificationSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {
    private final StringRedisTemplate redisTemplate;

    public void publish(NotificationSendRequest notificationSendRequest) {
        String message = convertToJson(notificationSendRequest);
        redisTemplate.convertAndSend("notifications", message);

        // Redis TTL 적용 (7일 후 자동 삭제)
//        ValueOperations<String, String> ops = redisTemplate.opsForValue();
//        String key = "notification:" + notificationSendRequest.getReceiverId();
//        ops.set(key, message, Duration.ofDays(7));
    }

    private String convertToJson(NotificationSendRequest notificationSendRequest) {
        try {
            return new ObjectMapper().writeValueAsString(notificationSendRequest);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingCustomException(e);
        }
    }
}

