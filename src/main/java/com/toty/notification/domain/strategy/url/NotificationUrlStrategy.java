package com.toty.notification.domain.strategy.url;

import com.toty.notification.domain.type.EventType;

public interface NotificationUrlStrategy {
    EventType getEventType();
    String generateUrl(String referenceId);
}