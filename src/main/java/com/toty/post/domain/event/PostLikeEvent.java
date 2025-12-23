package com.toty.post.domain.event;

import com.toty.post.dto.PostLikeDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostLikeEvent {
    private String type; // "LIKE", "UNLIKE"
    private PostLikeDto postLikeDto; // PostLike 엔티티 대신 PostLikeDto 참조
    private Long currentLikeCount; // 게시글의 현재 좋아요 수
    private LocalDateTime timestamp;

    public PostLikeEvent(String type, PostLikeDto postLikeDto, Long currentLikeCount) {
        this(type, postLikeDto, currentLikeCount, LocalDateTime.now());
    }
}
