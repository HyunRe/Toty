package com.toty.sms.presentation.response;

import com.toty.sms.application.SmsService.PostCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsResponse {

    private String id;

    private String nickname;

    private PostCategory board;

    public SmsResponse(String id, String nickname, PostCategory board) {
        this.id = id;
        this.nickname = nickname;
        this.board = board;
    }
}
