package com.toty.springconfig.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.common.baseException.JsonProcessingCustomException;
import com.toty.notification.application.sender.NotificationSenderService;
import com.toty.notification.dto.request.NotificationSendRequest;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {
    private final NotificationSenderService notificationSenderService;

    @Override
    public void onMessage(Message message, @NotNull byte[] pattern) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        NotificationSendRequest notificationSendRequest = convertFromJson(body);

        // 다른 서버에서는 생성하지 않고 전송만 수행
        try {
            notificationSenderService.send(notificationSendRequest);
        } catch (FirebaseMessagingException | MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private NotificationSendRequest convertFromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, NotificationSendRequest.class);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingCustomException(e);
        }
    }
}
