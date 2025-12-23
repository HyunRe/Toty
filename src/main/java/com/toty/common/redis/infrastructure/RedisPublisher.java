package com.toty.common.redis.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toty.common.baseException.JsonProcessingCustomException;
import com.toty.notification.application.service.NotificationPublisher;
import com.toty.notification.dto.request.NotificationSendRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPublisher implements NotificationPublisher {
    private final StringRedisTemplate redisTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void publish(NotificationSendRequest notificationSendRequest) {
        log.info("[REDIS-PUB] Redis 발행 시작: receiverId={}, eventType={}",
                notificationSendRequest.getReceiverId(), notificationSendRequest.getEventType());
        String message = convertToJson(notificationSendRequest);
        log.info("[REDIS-PUB] JSON 변환 완료, 메시지 길이: {}", message.length());
        redisTemplate.convertAndSend("notifications", message);
        log.info("[REDIS-PUB] ✅ Redis 발행 완료");
    }

    private String convertToJson(NotificationSendRequest notificationSendRequest) {
        try {
            return objectMapper.writeValueAsString(notificationSendRequest);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingCustomException(e);
        }
    }
}

