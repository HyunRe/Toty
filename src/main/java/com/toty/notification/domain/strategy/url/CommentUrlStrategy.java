package com.toty.notification.domain.strategy.url;

import com.toty.notification.domain.type.EventType;
import org.springframework.stereotype.Component;

@Component
public class CommentUrlStrategy implements NotificationUrlStrategy {
    @Override
    public EventType getEventType() {
        return EventType.COMMENT;
    }

    @Override
    public String generateUrl(String referenceId) {
        return "/view/posts/" + referenceId + "/detail";
    }
}