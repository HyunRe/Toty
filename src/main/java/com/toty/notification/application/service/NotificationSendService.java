package com.toty.notification.application.service;

import com.toty.notification.domain.eventHandler.NotificationEvent;
import com.toty.notification.dto.request.NotificationSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSendService {
    private final ApplicationEventPublisher eventPublisher;

    // 각 도메인 패키지에서 이벤트 처리 필요
    public void sendNotification(NotificationSendRequest notificationSendRequest) {
        eventPublisher.publishEvent(new NotificationEvent(this, notificationSendRequest));
    }
}
