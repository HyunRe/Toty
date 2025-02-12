package com.toty.post.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.toty.common.baseException.UnknownCategoryException;
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

    @JsonValue
    public String getCategory() {
        return category;
    }

    @JsonCreator
    public static PostCategory fromString(String category) {
        for (PostCategory postCategory : PostCategory.values()) {
            if (postCategory.category.equalsIgnoreCase(category)) {
                return postCategory;
            }
        }
        throw new UnknownCategoryException(category);
    }
}
