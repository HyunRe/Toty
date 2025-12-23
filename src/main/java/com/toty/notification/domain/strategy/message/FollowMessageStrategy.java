package com.toty.notification.domain.strategy.message;

import com.toty.notification.domain.type.EventType;
import org.springframework.stereotype.Component;

@Component
public class FollowMessageStrategy implements NotificationMessageStrategy {
    @Override
    public EventType getEventType() {
        return EventType.FOLLOW;
    }

    @Override
    public String generateMessage(String senderNickname) {
        return senderNickname + "님이 나를 팔로우했습니다.";
    }
}