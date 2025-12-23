package com.toty.notification.domain.eventHandler;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.notification.application.sender.NotificationSenderService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// 누군가 나를 팔로우 한 알림 처리
@Component
@RequiredArgsConstructor
public class FollowNotificationHandler {
    private final NotificationSenderService notificationSenderService;

    @EventListener
    public void handleNotification(NotificationEvent event) throws MessagingException, FirebaseMessagingException {
        if ("Follow".equals(event.getNotificationSendRequest().getType())) {
            notificationSenderService.send(event.getNotificationSendRequest());
        }
    }
}
