package com.toty.notification.application.sender;

import com.toty.notification.application.service.SmsService;
import com.toty.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsNotificationSender implements NotificationSender {
    private final SmsService smsService;

    @Override
    public void send(Notification notification) {
        smsService.sendSmsNotification(notification);
    }
}
