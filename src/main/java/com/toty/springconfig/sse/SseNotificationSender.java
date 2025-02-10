package com.toty.springconfig.sse;

import com.toty.notification.application.sender.NotificationSender;
import com.toty.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class SseNotificationSender implements NotificationSender {
    private final SseService sseService;

    @Override
    public void send(Notification notification) {
        SseNotificationSendRequest sseNotificationSendRequest = new SseNotificationSendRequest(
            notification.getSenderNickname(),
            notification.getMessage(),
            notification.getUrl()
        );

        // 알림 발생 시 해당 사용자에게 토스트 알림을 전송
        sseService.sendNotification(sseNotificationSendRequest);

        // 다중 알림 발생 시 해당 사용자들에게 토스트 알림을 전송
        sseService.sendMultipleNotifications(Collections.singletonList(sseNotificationSendRequest));
    }
}