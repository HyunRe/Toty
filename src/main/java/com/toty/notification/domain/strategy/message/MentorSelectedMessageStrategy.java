package com.toty.notification.domain.strategy.message;

import org.springframework.stereotype.Component;

@Component
public class MentorSelectedMessageStrategy implements NotificationMessageStrategy {
    @Override
    public String generateMessage(String senderNickname) {
        return "축하합니다!" + senderNickname + "님은 멘토로 선정되었습니다.";
    }
}