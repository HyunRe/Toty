package com.toty.notification.domain.strategy.url;

import org.springframework.stereotype.Component;

@Component
public class MentorPostUrlStrategy implements NotificationUrlStrategy {
    @Override
    public String generateUrl(String referenceId) {
        return "/view/posts/" + referenceId + "/detail?postCategory=knowledge";
    }
}