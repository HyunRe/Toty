package com.toty.notification.domain.strategy.url;

import org.springframework.stereotype.Component;

@Component
public class MentorChatUrlStrategy implements NotificationUrlStrategy {
    @Override
    public String generateUrl(String referenceId) {
        return "/chat/room/" + referenceId;
    }
}
