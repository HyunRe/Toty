package com.toty.notification.application.sender;

import com.toty.notification.application.service.EmailService;
import com.toty.notification.domain.model.Notification;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {
    private final EmailService emailService;

    @Override
    public void send(Notification notification) throws MessagingException {
        emailService.sendEmailNotification(notification);
    }
}

