package com.toty.notification.domain.strategy.message;

import com.toty.notification.domain.type.EventType;
import org.springframework.stereotype.Component;

@Component
public class MentorChatMessageStrategy implements NotificationMessageStrategy {
    @Override
    public EventType getEventType() {
        return EventType.MENTOR_CHAT;
    }

    @Override
    public String generateMessage(String senderNickname) {
        return "내가 팔로우한 멘토 " + senderNickname + "님이 새로운 채팅방을 개설했습니다.";
    }
}

