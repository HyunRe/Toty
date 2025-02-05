package com.toty.notification.application.strategy.url;

import org.springframework.stereotype.Component;

@Component
public class CommentUrlStrategy implements NotificationUrlStrategy {
    @Override
    public String generateUrl(String referenceId) {
        return "/view/posts/" + referenceId + "/detail";
    }
}