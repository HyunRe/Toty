package com.toty.notification.domain.strategy.message;

import com.toty.notification.domain.type.EventType;
import org.springframework.stereotype.Component;

@Component
public class BecomeMentorMessageStrategy implements NotificationMessageStrategy {
    @Override
    public EventType getEventType() {
        return EventType.BECOME_MENTOR;
    }

    @Override
    public String generateMessage(String senderNickname) {
        return "축하합니다! 멘토로 선정되었습니다!";
    }
}