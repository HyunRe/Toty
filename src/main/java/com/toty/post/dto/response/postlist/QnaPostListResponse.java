package com.toty.post.dto.response.postlist;

import com.toty.post.domain.model.PostTag;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QnaPostListResponse extends PostListResponse {
    private List<PostTag> postTags;
    private int commentCount;

    public QnaPostListResponse(Long id, String nickname, String profileImageUrl, String title, int viewCount,
                               int likeCount, LocalDateTime earliestTime, List<PostTag> postTags, int commentCount) {
        super(id, nickname, profileImageUrl, title, viewCount, likeCount, earliestTime);
        this.postTags = postTags;
        this.commentCount = commentCount;
    }
}
