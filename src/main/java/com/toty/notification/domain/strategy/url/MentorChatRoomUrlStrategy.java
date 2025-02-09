package com.toty.notification.domain.strategy.url;

import org.springframework.stereotype.Component;

@Component
public class MentorChatRoomUrlStrategy implements NotificationUrlStrategy {
    @Override
    public String generateUrl(String referenceId) {
        return "/chat/room/" + referenceId;
    }
}
