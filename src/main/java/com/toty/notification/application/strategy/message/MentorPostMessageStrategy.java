package com.toty.notification.application.strategy.message;

import org.springframework.stereotype.Component;

@Component
public class MentorPostMessageStrategy implements NotificationMessageStrategy {
    @Override
    public String generateMessage(String senderNickname) {
        return "내가 팔로우한 멘토 " + senderNickname + "님이 지식 게사글을 작성했습니다.";
    }
}
