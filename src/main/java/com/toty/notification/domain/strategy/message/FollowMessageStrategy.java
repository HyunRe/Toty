package com.toty.notification.domain.strategy.message;

import org.springframework.stereotype.Component;

@Component
public class FollowMessageStrategy implements NotificationMessageStrategy {
    @Override
    public String generateMessage(String senderNickname) {
        return senderNickname + "님이 나를 팔로우했습니다.";
    }
}