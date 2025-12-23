package com.toty.notification.domain.strategy.message;

import com.toty.notification.domain.type.EventType;
import org.springframework.stereotype.Component;

@Component
public class RevokeMentorMessageStrategy  implements NotificationMessageStrategy {
    @Override
    public EventType getEventType() {
        return EventType.REVOKE_MENTOR;
    }

    @Override
    public String generateMessage(String senderNickname) {
        return "아쉽습니다.. 이번에는 멘토로 선정되지 못했습니다 ㅠㅠ";
    }
}
