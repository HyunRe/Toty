package com.toty.sms.presentation.request;

import com.toty.sms.application.SmsService.PostCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostReplyNotificationRequest {

    private Long id;

    private String nickname; // 자유, 정보 / 질문

    private PostCategory board; // 자유, 정보 / 질문

    private Type type; // 자유, 정보

    private String title;
    // 자유, 정보, 질문
    public PostReplyNotificationRequest(Long id, String nickname, PostCategory board, Type type, String title) {
        this.id = id;
        this.nickname = nickname;
        this.board = board;
        this.type = type;
        this.title = title;
    }

    public enum Type {
        REPLY, LIKE
    }

}
