package com.toty.notification.application.strategy.url;

import org.springframework.stereotype.Component;

@Component
public class MentorChatUrlStrategy implements NotificationUrlStrategy {
    @Override
    public String generateUrl(String referenceId) {
        return "/chat/room/" + referenceId;
    }
}
