package com.toty.notification.domain.strategy.message;


import org.springframework.stereotype.Component;

@Component
public class QnaCommentMessageStrategy implements NotificationMessageStrategy {
    @Override
    public String generateMessage(String senderNickname) {
        return senderNickname + "님이 내 질문 게시글에 댓글을 남겼습니다.";
    }
}
