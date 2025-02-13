package com.toty.post.dto.response.postdetail;

import com.toty.common.domain.BaseTime;
import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.PostCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PostDetailResponse extends BaseTime {
    // 사용자 정보
    private String nickname;
    private String profileImageUrl;

    // 게시글 정보
    private PostCategory postCategory;
    private String title;
    private String content;
    private int viewCount;
    private int likeCount;
    private Boolean isLiked;
    private LocalDateTime earliestTime;

    // 댓글 정보
    private PaginationResult comments;

    public PostDetailResponse(String nickname, String profileImageUrl, PostCategory postCategory, String title, String content, int viewCount, int likeCount, Boolean isLiked, LocalDateTime earliestTime, PaginationResult comments) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.postCategory = postCategory;
        this.title = title;
        this.content = content;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
        this.earliestTime = earliestTime;
        this.comments = comments;
    }
}
