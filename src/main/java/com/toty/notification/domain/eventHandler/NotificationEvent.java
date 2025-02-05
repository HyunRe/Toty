package com.toty.notification.domain.eventHandler;

import com.toty.notification.dto.request.NotificationSendRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class NotificationEvent extends ApplicationEvent {
    private final NotificationSendRequest notificationSendRequest;

    public NotificationEvent(Object source, NotificationSendRequest notificationSendRequest) {
        super(source);
        this.notificationSendRequest = notificationSendRequest;
    }
}
