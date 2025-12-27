package com.toty.notification.domain.strategy.url;

import com.toty.notification.domain.type.EventType;
import org.springframework.stereotype.Component;

@Component
public class RevokeMentorUrlStrategy implements NotificationUrlStrategy {
    @Override
    public EventType getEventType() {
        return EventType.REVOKE_MENTOR;
    }

    // 로컬
//    @Override
//    public String generateUrl(String referenceId) {
//        return "/view/users/home";
//    }

    // 서버
    @Override
    public String generateUrl(String referenceId) {
        return "/view/users/home";
    }
}
