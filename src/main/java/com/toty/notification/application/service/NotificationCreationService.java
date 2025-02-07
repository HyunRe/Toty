package com.toty.notification.application.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.notification.domain.factory.message.NotificationMessageFactory;
import com.toty.notification.domain.factory.url.NotificationUrlFactory;
import com.toty.notification.application.sender.NotificationSenderService;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.domain.repository.NotificationRepository;
import com.toty.notification.dto.request.NotificationSendRequest;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationCreationService {
    private final NotificationRepository notificationRepository;
    private final NotificationSenderService notificationSenderService;
    private final NotificationMessageFactory notificationMessageFactory;
    private final NotificationUrlFactory notificationUrlFactory;

    public void createNotification(NotificationSendRequest notificationSendRequest) throws FirebaseMessagingException, MessagingException {
        String message = notificationMessageFactory.generateMessage(
                notificationSendRequest.getType(),
                notificationSendRequest.getSenderNickname()
        );

        String url = notificationUrlFactory.generateUrl(
                notificationSendRequest.getType(),
                notificationSendRequest.getReferenceId()
        );

        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                notificationSendRequest.getReceiverId(),
                notificationSendRequest.getSenderId(),
                notificationSendRequest.getSenderNickname(),
                notificationSendRequest.getType(),
                message,
                url,
                false,
                LocalDateTime.now()
        );

        // Redis에 저장
        notificationRepository.save(notification);

        // 실시간 전송 (SSE 또는 Pub/Sub)
        notificationSenderService.send(notification);
    }
}
