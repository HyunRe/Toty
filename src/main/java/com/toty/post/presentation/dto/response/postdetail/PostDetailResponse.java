package com.toty.post.presentation.dto.response.postdetail;

import com.toty.base.domain.model.BaseTime;
import com.toty.post.domain.model.PostCategory;
import com.toty.post.domain.model.PostImage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<PostImage> postImages;
    private int viewCount;
    private int likeCount;
    private LocalDateTime earliestTime;

    public PostDetailResponse(String nickname, String profileImageUrl, PostCategory postCategory, String title, String content, List<PostImage> postImages, int viewCount, int likeCount, LocalDateTime earliestTime) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.postCategory = postCategory;
        this.title = title;
        this.content = content;
        this.postImages = postImages;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.earliestTime = earliestTime;
    }
}
