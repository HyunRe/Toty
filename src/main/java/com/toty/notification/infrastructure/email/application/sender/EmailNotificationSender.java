package com.toty.notification.infrastructure.email.application.sender;

import com.toty.notification.application.sender.NotificationSender;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.infrastructure.email.application.service.EmailService;
import com.toty.notification.infrastructure.email.dto.EmailNotificationSendRequest;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {
    private final EmailService emailService;

    @Override
    public void send(Notification notification) throws MessagingException {
        EmailNotificationSendRequest emailNotificationSendRequest = new EmailNotificationSendRequest(
                notification.getReceiverId(),
                notification.getMessage(),
                notification.getEventType()
        );

        emailService.sendEmailNotification(emailNotificationSendRequest);
    }
}

