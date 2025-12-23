package com.toty.notification.domain.eventHandler;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.notification.application.sender.NotificationSenderService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// 게시글에 졸아요 알림 처리
@Component
@RequiredArgsConstructor
public class PostLikeHandler {
    private final NotificationSenderService notificationSenderService;

    @EventListener
    public void handleNotification(NotificationEvent event) throws MessagingException, FirebaseMessagingException {
        if ("Like".equals(event.getNotificationSendRequest().getType())) {
            notificationSenderService.send(event.getNotificationSendRequest());
        }
    }
}
