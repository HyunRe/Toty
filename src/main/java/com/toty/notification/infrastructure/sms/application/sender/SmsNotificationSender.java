package com.toty.notification.infrastructure.sms.application.sender;

import com.toty.notification.application.sender.NotificationSender;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.infrastructure.sms.application.service.SmsService;
import com.toty.notification.infrastructure.sms.dto.SmsNotificationSendRequest;
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
