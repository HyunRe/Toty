package com.toty.notification.application.sender;

import com.toty.notification.domain.model.Notification;
import com.toty.notification.application.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class SseNotificationSender implements NotificationSender {
    private final SseService sseService;

    @Override
    public void send(Notification notification) {
        // 알림 발생 시 해당 사용자에게 알림을 전송
        sseService.sendNotification(notification);

        // 다중 알림 발생 시 해당 사용자들에게 알림을 전송
        sseService.sendMultipleNotifications(Collections.singletonList(notification));
    }
}