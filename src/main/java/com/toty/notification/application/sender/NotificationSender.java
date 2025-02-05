package com.toty.notification.application.sender;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.notification.domain.model.Notification;
import jakarta.mail.MessagingException;

public interface NotificationSender {
    void send(Notification notification) throws FirebaseMessagingException, MessagingException;
}
