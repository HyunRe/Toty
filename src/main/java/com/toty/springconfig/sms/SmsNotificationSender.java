package com.toty.springconfig.sms;

import com.toty.notification.application.sender.NotificationSender;
import com.toty.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsNotificationSender implements NotificationSender {
    private final SmsService smsService;

    @Override
    public void send(Notification notification) {
        SmsNotificationSendRequest smsNotificationSendRequest = new SmsNotificationSendRequest(
                notification.getReceiverId(),
                notification.getMessage(),
                notification.getUrl()
        );

        smsService.sendSmsNotification(smsNotificationSendRequest);
    }
}
