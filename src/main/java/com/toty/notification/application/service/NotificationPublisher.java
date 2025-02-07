package com.toty.notification.application.service;

import com.toty.notification.dto.request.NotificationSendRequest;

public interface NotificationPublisher {
    void publish(NotificationSendRequest notificationSendRequest);
}
