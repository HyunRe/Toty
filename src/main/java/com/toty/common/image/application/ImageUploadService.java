package com.toty.common.image.application;

import com.toty.common.image.domain.component.ImageValidator;
import com.toty.common.image.domain.component.S3KeyGenerator;
import com.toty.common.image.domain.model.Image;
import com.toty.common.image.domain.model.ImageType;
import com.toty.common.image.domain.repository.ImageRepository;
import com.toty.common.image.infrastructure.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageUploadService {
    private final S3StorageService s3StorageService;
    private final S3KeyGenerator s3KeyGenerator;
    private final ImageValidator imageValidator;
    private final ImageRepository imageRepository;

    /**
     * User 프로필 이미지 업로드
     * 기존 프로필 이미지가 있으면 S3와 DB에서 삭제 후 새로운 이미지 업로드
     */
    @Transactional
    public String uploadForProfile(Long userId, MultipartFile file) {
        // 기존 프로필 이미지 삭제
        List<Image> existingImages = imageRepository.findByUserIdAndType(userId, ImageType.PROFILE);
        for (Image existingImage : existingImages) {
            s3StorageService.deleteFile(existingImage.getPath());
            imageRepository.delete(existingImage);
        }

        // 새 프로필 이미지 업로드
        String key = s3KeyGenerator.generateKey("profile", userId, file.getOriginalFilename());
        String url = s3StorageService.uploadFile(key, file);

        // DB에 메타데이터 저장
        Image image = new Image(key, url, userId, null, ImageType.PROFILE, null);
        imageRepository.save(image);

        return url;
    }

    /**
     * Post Summernote 에디터 이미지 업로드
     */
    @Transactional
    public String uploadForSummernote(Long userId, MultipartFile file) {
        imageValidator.validateImageCount(userId, 1);
        String key = s3KeyGenerator.generateKey("post", userId, file.getOriginalFilename());
        String url = s3StorageService.uploadFile(key, file);

        // DB에 메타데이터 저장 (postId는 나중에 게시글 저장 시 연결)
        Image image = new Image(key, url, userId, null, ImageType.POST_CONTENT, null);
        imageRepository.save(image);

        return url;
    }

    /**
     * 특정 사용자의 프로필 이미지를 조회합니다.
     * @param userId 사용자 ID
     * @return 프로필 이미지 URL (없으면 null)
     */
    public String getProfileImageUrl(Long userId) {
        return imageRepository.findByUserIdAndType(userId, ImageType.PROFILE)
                .stream()
                .findFirst()
                .map(Image::getUrl)
                .orElse(null);
    }

    /**
     * 특정 게시글에 포함된 이미지 리스트를 조회합니다.
     * @param postId 게시글 ID
     * @return 이미지 리스트
     */
    public List<Image> getPostImages(Long postId) {
        return imageRepository.findByPostId(postId);
    }

    /**
     * 게시글 삭제 시 해당 게시글의 모든 이미지를 S3와 DB에서 삭제합니다.
     * @param postId 삭제할 게시글 ID
     */
    @Transactional
    public void deletePostImages(Long postId) {
        List<Image> images = imageRepository.findByPostId(postId);
        for (Image image : images) {
            s3StorageService.deleteFile(image.getPath());
            imageRepository.delete(image);
        }
    }

    /**
     * Post 저장 후 이미지와 postId 연결
     *
     * 사용 시나리오:
     * 1. 사용자가 Summernote 에디터에서 이미지를 업로드하면 uploadForSummernote()가 호출되어 S3에 저장
     * 2. 이때 postId는 아직 없으므로 null로 저장됨
     * 3. 사용자가 게시글 작성을 완료하고 "저장" 버튼을 누름
     * 4. PostService에서 Post를 저장한 후, content HTML에서 이미지 URL들을 추출
     * 5. 이 메서드를 호출하여 해당 이미지들의 postId를 업데이트
     *
     * 예제:
     * // PostService.java
     * Post savedPost = postRepository.save(post);
     * List<String> imageUrls = extractImageUrlsFromHtml(post.getContent());
     * imageUploadService.linkImagesToPost(savedPost.getId(), savedPost.getUser().getId(), imageUrls);
     *
     * @param postId 저장된 게시글 ID
     * @param userId 게시글 작성자 ID
     * @param imageUrls content HTML에 포함된 이미지 URL 리스트
     */
    @Transactional
    public void linkImagesToPost(Long postId, Long userId, List<String> imageUrls) {
        for (String url : imageUrls) {
            // userId와 url로 이미지를 찾고, postId가 null인 것만 업데이트
            imageRepository.findByUserId(userId).stream()
                .filter(img -> img.getUrl().equals(url) && img.getPostId() == null)
                .findFirst()
                .ifPresent(img -> img.setPostId(postId));
        }
    }
}
