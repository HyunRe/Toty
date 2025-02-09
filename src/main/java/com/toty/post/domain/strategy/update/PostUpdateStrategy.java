package com.toty.post.domain.strategy.update;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.post.application.PostImageService;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostCategory;
import com.toty.post.dto.request.PostUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PostUpdateStrategy {
    Post updatePostRequest(PostUpdateRequest postUpdateRequest, Post post);

    PostCategory getPostCategory();

    // 이미지 업로드 처리 (기본 메소드)
    default void synchronizeImages(Post post, List<MultipartFile> images, PostImageService postImageService) {
        // 이미지 형식 검증
        validateImageFormats(images);

        try {
            postImageService.updateImages(post, images);
        } catch (IOException e) {
            throw new ExpectedException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    // 이미지 형식 검증 (기본 메소드)
    default void validateImageFormats(List<MultipartFile> images) {
        images.forEach(image -> {
            if (isValidImageFormat(image.getOriginalFilename())) {
                throw new ExpectedException(ErrorCode.INVALID_IMAGE_FORMAT);
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