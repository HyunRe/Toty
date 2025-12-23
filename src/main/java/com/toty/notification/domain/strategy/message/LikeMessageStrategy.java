package com.toty.notification.domain.strategy.message;

import com.toty.notification.domain.type.EventType;
import org.springframework.stereotype.Component;

@Component
public class LikeMessageStrategy implements NotificationMessageStrategy {
    @Override
    public EventType getEventType() {
        return EventType.LIKE;
    }

    @Override
    public String generateMessage(String senderNickname) {
        return senderNickname + "님이 내 게시글을 좋아합니다.";
    }
}