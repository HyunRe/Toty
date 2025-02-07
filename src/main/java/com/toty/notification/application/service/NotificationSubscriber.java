package com.toty.notification.application.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.mail.MessagingException;
import org.springframework.data.redis.connection.Message;

public interface NotificationSubscriber {
    void onMessage(Message message) throws FirebaseMessagingException, MessagingException;
}
