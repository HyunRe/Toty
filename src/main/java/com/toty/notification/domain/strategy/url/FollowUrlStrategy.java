package com.toty.notification.domain.strategy.url;

import com.toty.notification.domain.type.EventType;
import org.springframework.stereotype.Component;

@Component
public class FollowUrlStrategy implements NotificationUrlStrategy {
    @Override
    public EventType getEventType() {
        return EventType.FOLLOW;
    }

    @Override
    public String generateUrl(String referenceId) {
        return "/main/following/" + referenceId + "info";
    }
}