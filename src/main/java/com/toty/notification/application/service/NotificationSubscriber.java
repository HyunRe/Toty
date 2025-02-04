package com.toty.notification.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.base.exception.JsonProcessingCustomException;
import com.toty.notification.presentation.dto.request.NotificationSendRequest;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class NotificationSubscriber {
    private final NotificationService notificationService;

    @EventListener
    public void onMessage(Message message, byte[] pattern) throws FirebaseMessagingException, MessagingException {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        NotificationSendRequest notificationSendRequest = convertFromJson(body);
        notificationService.createNotification(notificationSendRequest);
    }

    private NotificationSendRequest convertFromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, NotificationSendRequest.class);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingCustomException(e);
        }
    }
}
