package com.toty.notification.domain.strategy.message;

import com.toty.notification.domain.type.EventType;
import org.springframework.stereotype.Component;

@Component
public class CommentMessageStrategy implements NotificationMessageStrategy {
    @Override
    public EventType getEventType() {
        return EventType.COMMENT;
    }

    @Override
    public String generateMessage(String senderNickname) {
        return senderNickname + "님이 내 게시글에 댓글을 남겼습니다.";
    }
}