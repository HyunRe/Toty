package com.toty.notification.domain.strategy.message;

import com.toty.notification.domain.type.EventType;

public interface NotificationMessageStrategy {
    EventType getEventType();
    String generateMessage(String senderNickname);
}
