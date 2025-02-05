package com.toty.notification.domain.strategy.url;

import org.springframework.stereotype.Component;

@Component
public class FollowUrlStrategy implements NotificationUrlStrategy {
    @Override
    public String generateUrl(String referenceId) {
        return "/main/following/" + referenceId;
    }
}