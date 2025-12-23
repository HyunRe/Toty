package com.toty.notification.domain.eventHandler;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.notification.application.sender.NotificationSenderService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// 내가 팔로우 한 멘토가 채팅방 개설 알림 처리
@Component
@RequiredArgsConstructor
public class MentorChatRoomNotificationHandler {
    private final NotificationSenderService notificationSenderService;

    @EventListener
    public void handleNotification(NotificationEvent event) throws MessagingException, FirebaseMessagingException {
        if ("ChatRoom".equals(event.getNotificationSendRequest().getType())) {
            notificationSenderService.send(event.getNotificationSendRequest());
        }
    }
}
