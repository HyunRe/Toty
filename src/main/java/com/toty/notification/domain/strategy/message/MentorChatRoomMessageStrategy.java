package com.toty.notification.domain.strategy.message;

import org.springframework.stereotype.Component;

@Component
public class MentorChatRoomMessageStrategy implements NotificationMessageStrategy {
    @Override
    public String generateMessage(String senderNickname) {
        return "내가 팔로우한 멘토 " + senderNickname + "님이 새로운 채팅방을 개설했습니다.";
    }
}

