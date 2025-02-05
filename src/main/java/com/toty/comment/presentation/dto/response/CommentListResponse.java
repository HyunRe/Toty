package com.toty.comment.presentation.dto.response;

import com.toty.common.domain.BaseTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentListResponse extends BaseTime {
    // 사용자 정보
    private String nickname;
    private String profileImageUrl;

    // 댓글 정보
    private String content;
    private LocalDateTime earliestTime;

    public CommentListResponse(String nickname, String profileImageUrl, String content, LocalDateTime earliestTime) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.content = content;
        this.earliestTime = earliestTime;
    }
}
