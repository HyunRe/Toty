package com.toty.sms.presentation.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsChatRoomNotificationRequest{

    private Long mentorId; // 멘토 아이디

    private String title; // 채팅방 이름

    public SmsChatRoomNotificationRequest(Long id,
            String title) {
        this.mentorId = id;
        this.title = title;
    }
}
