package com.toty.notification.application.strategy.message;

import org.springframework.stereotype.Component;

@Component
public class CommentMessageStrategy implements NotificationMessageStrategy {
    @Override
    public String generateMessage(String senderNickname) {
        return senderNickname + "님이 내 게시글에 댓글을 남겼습니다.";
    }
}