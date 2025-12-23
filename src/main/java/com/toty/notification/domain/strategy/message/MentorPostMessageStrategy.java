package com.toty.notification.domain.strategy.message;

import com.toty.notification.domain.type.EventType;
import org.springframework.stereotype.Component;

@Component
public class MentorPostMessageStrategy implements NotificationMessageStrategy {
    @Override
    public EventType getEventType() {
        return EventType.MENTOR_POST;
    }

    @Override
    public String generateMessage(String senderNickname) {
        return "내가 팔로우한 멘토 " + senderNickname + "님이 지식 게사글을 작성했습니다.";
    }
}