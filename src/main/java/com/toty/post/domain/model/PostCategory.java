package com.toty.post.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum PostCategory {
    QnA("Qna"), KNOWLEDGE("Knowledge"), GENERAL("General");

    private String category;

    PostCategory(String category) {
        this.category = category;
    }
}
