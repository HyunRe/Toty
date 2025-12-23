package com.toty.comment.dto.response;

import com.toty.common.domain.BaseTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentListResponse extends BaseTime {
    // 댓글 ID
    private Long id;

    // 사용자 정보
    private Long authorId;
    private String authorNickname;
    private String authorProfileImageUrl;

    // 댓글 정보
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentListResponse(Long id, Long authorId, String authorNickname, String authorProfileImageUrl, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.authorId = authorId;
        this.authorNickname = authorNickname;
        this.authorProfileImageUrl = authorProfileImageUrl;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}