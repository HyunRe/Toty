package com.toty.post.dto.response.postdetail;

import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.PostCategory;
import com.toty.post.domain.model.PostTag;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QnaPostDetailResponse extends PostDetailResponse {
    private List<PostTag> postTags;

    public QnaPostDetailResponse(String nickname, String profileImageUrl, PostCategory postCategory, String title, String content,
                                 int viewCount, int likeCount, Boolean isLiked, List<PostTag> postTags, LocalDateTime earliestTime, PaginationResult comments) {
        super(nickname, profileImageUrl, postCategory, title, content, viewCount, likeCount, isLiked ,earliestTime, comments);
        this.postTags = postTags;

        System.out.println("postTags: " + postTags);
    }
}
