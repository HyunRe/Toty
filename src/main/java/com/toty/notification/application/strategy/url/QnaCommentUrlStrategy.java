package com.toty.notification.application.strategy.url;

import org.springframework.stereotype.Component;

@Component
public class QnaCommentUrlStrategy implements NotificationUrlStrategy {
    @Override
    public String generateUrl(String referenceId) {
        return "/view/posts/" + referenceId + "/detail?postCategory=qna";
    }
}
