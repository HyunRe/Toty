package com.toty.notification.application.service;

import com.toty.notification.domain.factory.message.NotificationMessageFactory;
import com.toty.notification.domain.factory.url.NotificationUrlFactory;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.domain.repository.NotificationRepository;
import com.toty.notification.dto.request.NotificationSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationCreationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMessageFactory notificationMessageFactory;
    private final NotificationUrlFactory notificationUrlFactory;

    public Notification createNotification(NotificationSendRequest notificationSendRequest) {
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

        // Redis DB에 저장
        notificationRepository.save(notification);

        return notification;
    }
}
