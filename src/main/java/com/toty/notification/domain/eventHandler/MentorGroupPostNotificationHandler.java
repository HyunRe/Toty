package com.toty.notification.domain.eventHandler;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.notification.application.service.NotificationCreationService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// 내가 팔로우 한 멘토가 지식 게시글 작성 단체 알림 처리
@Component
@RequiredArgsConstructor
public class MentorGroupPostNotificationHandler {
    private final NotificationCreationService notificationCreationService;

    @EventListener
    public void handleNotification(NotificationEvent event) throws MessagingException, FirebaseMessagingException {
        if ("GroupKnowledge".equals(event.getNotificationSendRequest().getType())) {
            notificationCreationService.createNotification(event.getNotificationSendRequest());
        }
    }
}
