package com.toty.notification.domain.strategy.url;

import com.toty.notification.domain.type.EventType;
import org.springframework.stereotype.Component;

@Component
public class BecomeMentorUrlStrategy implements NotificationUrlStrategy {
    @Override
    public EventType getEventType() {
        return EventType.BECOME_MENTOR;
    }

    @Override
    public String generateUrl(String referenceId) {
        return "http://localhost:8070/view/users/home";
    }
}
