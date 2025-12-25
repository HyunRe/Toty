package com.toty.common.image.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String path; // S3 Key (images/post/1/uuid.png)
    private String url;  // 전체 URL 또는 Presigned URL
    private Long userId; // 업로드한 사용자
    private Long postId; // 어느 게시글인지 (nullable, PROFILE 타입인 경우 null)

    @Enumerated(EnumType.STRING)
    private ImageType type; // PROFILE 또는 POST_CONTENT

    private LocalDateTime expiredAt; // 만료시간

    public Image(String path, String url, Long userId, Long postId, ImageType type, LocalDateTime expiredAt) {
        this.path = path;
        this.url = url;
        this.userId = userId;
        this.postId = postId;
        this.type = type;
        this.expiredAt = expiredAt;
    }

    public boolean isPresignedUrlValid() {
        return expiredAt != null && expiredAt.isAfter(LocalDateTime.now().plusMinutes(5));
    }

    public void updateUrl(String url, LocalDateTime expiredAt) {
        this.url = url;
        this.expiredAt = expiredAt;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }
}
