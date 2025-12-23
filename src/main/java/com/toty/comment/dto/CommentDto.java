package com.toty.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.toty.comment.domain.model.Comment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("postId")
    private Long postId; // 댓글이 달린 게시물 ID

    @JsonProperty("authorId")
    private Long authorId; // 댓글 작성자 ID

    @JsonProperty("authorNickname")
    private String authorNickname; // 댓글 작성자 닉네임

    @JsonProperty("authorProfileImageUrl")
    private String authorProfileImageUrl; // 작성자 프로필 이미지 URL

    @JsonProperty("content")
    private String content;

    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static CommentDto from(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.id = comment.getId();
        dto.postId = comment.getPost().getId();
        dto.authorId = comment.getUser().getId();
        dto.authorNickname = comment.getUser().getNickname();
        dto.authorProfileImageUrl = comment.getUser().getProfileImageUrl();
        dto.content = comment.getContent();
        dto.createdAt = comment.getCreatedAt();
        dto.updatedAt = comment.getUpdatedAt();

        // 디버그 로그
        log.debug("CommentDto created - ID: {}, createdAt: {}, updatedAt: {}",
                  dto.id, dto.createdAt, dto.updatedAt);

        return dto;
    }
}
