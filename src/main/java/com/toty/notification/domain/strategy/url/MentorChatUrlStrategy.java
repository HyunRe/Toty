package com.toty.notification.domain.strategy.url;

import com.toty.notification.domain.type.EventType;
import org.springframework.stereotype.Component;

@Component
public class MentorChatUrlStrategy implements NotificationUrlStrategy {
    @Override
    public EventType getEventType() {
        return EventType.MENTOR_CHAT;
    }

    @Override
    public String generateUrl(String referenceId) {
        return "/view/chatting/room?rid=" + referenceId;
    }
}
