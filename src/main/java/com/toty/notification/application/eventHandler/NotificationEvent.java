package com.toty.notification.application.eventHandler;

import com.toty.notification.presentation.dto.request.NotificationSendRequest;
import org.springframework.context.ApplicationEvent;


public class NotificationEvent extends ApplicationEvent {
    private final NotificationSendRequest notificationSendRequest;

    public NotificationEvent(Object source, NotificationSendRequest notificationSendRequest) {
        super(source);
        this.notificationSendRequest = notificationSendRequest;
    }

    public NotificationSendRequest getNotificationSendRequest() {
        return notificationSendRequest;
    }
}
