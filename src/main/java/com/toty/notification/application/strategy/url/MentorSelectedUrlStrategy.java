package com.toty.notification.application.strategy.url;

public class MentorSelectedUrlStrategy implements NotificationUrlStrategy {
    @Override
    public String generateUrl(String referenceId) {
        return "/main/main";
    }
}
