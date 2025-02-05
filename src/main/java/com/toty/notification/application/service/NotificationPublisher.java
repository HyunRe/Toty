package com.toty.notification.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toty.base.exception.JsonProcessingCustomException;
import com.toty.notification.presentation.dto.request.NotificationSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {
    private final StringRedisTemplate redisTemplate;

    public void publish(NotificationSendRequest notificationSendRequest) {
        String message = convertToJson(notificationSendRequest);
        redisTemplate.convertAndSend("notifications", message);
    }

    private String convertToJson(NotificationSendRequest notificationSendRequest) {
        try {
            return new ObjectMapper().writeValueAsString(notificationSendRequest);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingCustomException(e);
        }
    }
}

