package com.toty.notification.domain.strategy.message;

import org.springframework.stereotype.Component;

@Component
public class PostLikeMessageStrategy implements NotificationMessageStrategy {
    @Override
    public String generateMessage(String senderNickname) {
        return senderNickname + "님이 내 게시글을 좋아합니다.";
    }
}