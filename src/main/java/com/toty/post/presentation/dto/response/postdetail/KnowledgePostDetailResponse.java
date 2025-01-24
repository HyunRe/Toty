package com.toty.post.presentation.dto.response.postdetail;

import com.toty.post.domain.model.PostCategory;
import com.toty.post.domain.model.PostImage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgePostDetailResponse extends PostDetailResponse {
    // private Role role;

    public KnowledgePostDetailResponse(String nickname, String profileImageUrl, PostCategory postCategory,
                                       String title, String content, List<PostImage> postImages, int viewCount, int likeCount, LocalDateTime earliestTime) { // , Role role
        super(nickname, profileImageUrl, postCategory, title, content, postImages, viewCount, likeCount, earliestTime); // , Role role
        // this.role = role;
    }
}
