package com.toty.post.application.strategy.creation;

import com.toty.post.application.PostImageService;
import com.toty.post.domain.model.Post;
import com.toty.post.presentation.dto.request.PostCreateRequest;
import com.toty.user.domain.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PostCreationStrategy {
    Post createPostRequest(PostCreateRequest postCreateRequest, User user);

    // 댓글

    // 이미지 업로드 처리 (기본 메소드)
    default void processImages(Post post, List<MultipartFile> images, PostImageService postImageService) {
        // 이미지 형식 검증
        validateImageFormats(images);

        // 이미지 업로드 처리
        try {
            postImageService.uploadImage(post, images);
        } catch (IOException e) {
            throw new IllegalStateException("이미지 업로드 실패를 했습니다.", e); // 이미지 업로드 실패
        }
    }

    // 이미지 형식 검증 (기본 메소드)
    default void validateImageFormats(List<MultipartFile> images) {
        images.forEach(image -> {
            if (isValidImageFormat(image.getOriginalFilename())) {
                throw new IllegalArgumentException("잘못된 이미지 형식 입니다: " + image.getOriginalFilename());
            }
        });
    }

    // 이미지 형식 확인 (기본 메소드)
    default boolean isValidImageFormat(String imageFileName) {
        String extension = getFileExtension(imageFileName).toLowerCase();
        return !getValidImageExtensions().contains(extension);
    }

    // 파일 확장자 추출 (기본 메소드)
    default String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return "";
        }
        return fileName.substring(index + 1);
    }

    // 허용 되는 이미지 확장자 목록 (기본 메소드)
    default List<String> getValidImageExtensions() {
        return List.of("jpg", "jpeg", "png", "gif");
    }
}
