package com.toty.post.dto;

import com.toty.common.domain.DateTimeUtil;
import com.toty.post.domain.model.post.PostLike;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class PostLikeDto {
    private Long id; // 좋아요 자체의 ID
    private Long postId;
    private Long likerId; // 좋아요를 누른 사용자 ID
    private String likerNickname; // 좋아요를 누른 사용자 닉네임
    private String likerProfileImageUrl; // 좋아요를 누른 사용자 프로필 이미지
    private LocalDateTime createdAt;
    private String createdAtFormatted;   // "3분 전" 등

    public static PostLikeDto from(PostLike postLike) {
        PostLikeDto dto = new PostLikeDto();
        dto.id = postLike.getId();
        dto.postId = postLike.getPost().getId();
        dto.likerId = postLike.getUser().getId();
        dto.likerNickname = postLike.getUser().getNickname();
        dto.likerProfileImageUrl = postLike.getUser().getProfileImageUrl();
        dto.createdAt = postLike.getCreatedAt();
        dto.createdAtFormatted = DateTimeUtil.formatRelativeTime(postLike.getCreatedAt());
        return dto;
    }
}
