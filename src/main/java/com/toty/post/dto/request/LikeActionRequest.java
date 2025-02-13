package com.toty.post.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeActionRequest {
    private String likeAction;

    public LikeActionRequest(String likeAction) {
        this.likeAction = likeAction;
    }
}
