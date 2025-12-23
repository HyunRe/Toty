package com.toty.notification.application.event;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.notification.application.sender.NotificationSenderService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationSenderService notificationSenderService;

    @Async("notificationExecutor")
    @EventListener
    public void onNotificationEvent(NotificationEvent event) throws MessagingException, FirebaseMessagingException {
        try {
            log.info("알림 이벤트 수신: receiverId={}, eventType={}",
                event.getNotificationSendRequest().getReceiverId(),
                event.getNotificationSendRequest().getEventType());
            notificationSenderService.send(event.getNotificationSendRequest());
            log.info("알림 전송 완료: receiverId={}, eventType={}",
                event.getNotificationSendRequest().getReceiverId(),
                event.getNotificationSendRequest().getEventType());
        } catch (Exception e) {
            log.error("알림 전송 실패: receiverId={}, eventType={}, error={}",
                event.getNotificationSendRequest().getReceiverId(),
                event.getNotificationSendRequest().getEventType(),
                e.getMessage(), e);
        }
    }
}

