package com.toty.notification.domain.strategy.url;

public class MentorSelectedUrlStrategy implements NotificationUrlStrategy {
    @Override
    public String generateUrl(String referenceId) {
        return "/main";
    }
}
