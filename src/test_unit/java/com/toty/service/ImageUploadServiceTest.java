package com.toty.service;

import com.toty.common.image.application.ImageUploadService;
import com.toty.common.image.domain.component.ImageValidator;
import com.toty.common.image.domain.component.S3KeyGenerator;
import com.toty.common.image.domain.model.Image;
import com.toty.common.image.domain.model.ImageType;
import com.toty.common.image.domain.repository.ImageRepository;
import com.toty.common.image.infrastructure.S3StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImageUploadService 단위 테스트
 * 목적: S3 업로드/삭제 로직을 Mock으로 검증
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("이미지 업로드 서비스 단위 테스트")
class ImageUploadServiceTest {

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private S3KeyGenerator s3KeyGenerator;

    @Mock
    private ImageValidator imageValidator;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageUploadService imageUploadService;

    @Mock
    private MultipartFile mockFile;

    private Long userId;
    private String s3Key;
    private String s3Url;

    @BeforeEach
    void setUp() {
        userId = 1L;
        s3Key = "images/profile/1/uuid-test.jpg";
        s3Url = "https://s3.amazonaws.com/bucket/" + s3Key;
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공 - 기존 이미지 없음")
    void uploadForProfile_success_noExistingImage() {
        // given
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(imageRepository.findByUserIdAndType(userId, ImageType.PROFILE))
                .thenReturn(Collections.emptyList());
        when(s3KeyGenerator.generateKey("profile", userId, "test.jpg"))
                .thenReturn(s3Key);
        when(s3StorageService.uploadFile(s3Key, mockFile))
                .thenReturn(s3Url);

        // when
        String result = imageUploadService.uploadForProfile(userId, mockFile);

        // then
        assertThat(result).isEqualTo(s3Url);

        // S3 업로드 검증
        verify(s3StorageService).uploadFile(s3Key, mockFile);
        verify(s3StorageService, never()).deleteFile(anyString());

        // DB 저장 검증
        verify(imageRepository).save(argThat(image ->
                image.getPath().equals(s3Key) &&
                        image.getUrl().equals(s3Url) &&
                        image.getUserId().equals(userId) &&
                        image.getType() == ImageType.PROFILE
        ));
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공 - 기존 이미지 삭제 후 업로드")
    void uploadForProfile_success_replacesExistingImage() {
        // given
        String oldKey = "images/profile/1/old-uuid.jpg";
        String oldUrl = "https://s3.amazonaws.com/bucket/" + oldKey;
        Image existingImage = new Image(oldKey, oldUrl, userId, null, ImageType.PROFILE, null);

        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(imageRepository.findByUserIdAndType(userId, ImageType.PROFILE))
                .thenReturn(List.of(existingImage));
        when(s3KeyGenerator.generateKey("profile", userId, "test.jpg"))
                .thenReturn(s3Key);
        when(s3StorageService.uploadFile(s3Key, mockFile))
                .thenReturn(s3Url);

        // when
        String result = imageUploadService.uploadForProfile(userId, mockFile);

        // then
        assertThat(result).isEqualTo(s3Url);

        // 기존 이미지 삭제 검증
        verify(s3StorageService).deleteFile(oldKey);
        verify(imageRepository).delete(existingImage);

        // 새 이미지 업로드 검증
        verify(s3StorageService).uploadFile(s3Key, mockFile);
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공 - 여러 개의 기존 이미지 삭제 후 업로드")
    void uploadForProfile_success_deletesMultipleExistingImages() {
        // given
        Image existingImage1 = new Image("key1", "url1", userId, null, ImageType.PROFILE, null);
        Image existingImage2 = new Image("key2", "url2", userId, null, ImageType.PROFILE, null);

        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(imageRepository.findByUserIdAndType(userId, ImageType.PROFILE))
                .thenReturn(Arrays.asList(existingImage1, existingImage2));
        when(s3KeyGenerator.generateKey("profile", userId, "test.jpg"))
                .thenReturn(s3Key);
        when(s3StorageService.uploadFile(s3Key, mockFile))
                .thenReturn(s3Url);

        // when
        imageUploadService.uploadForProfile(userId, mockFile);

        // then
        // 모든 기존 이미지 삭제 검증
        verify(s3StorageService).deleteFile("key1");
        verify(s3StorageService).deleteFile("key2");
        verify(imageRepository).delete(existingImage1);
        verify(imageRepository).delete(existingImage2);

        // 새 이미지 업로드 검증
        verify(s3StorageService).uploadFile(s3Key, mockFile);
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    @DisplayName("Summernote 이미지 업로드 성공")
    void uploadForSummernote_success() {
        // given
        String postKey = "images/post/1/uuid-test.jpg";
        String postUrl = "https://s3.amazonaws.com/bucket/" + postKey;

        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        doNothing().when(imageValidator).validateImageCount(userId, 1);
        when(s3KeyGenerator.generateKey("post", userId, "test.jpg"))
                .thenReturn(postKey);
        when(s3StorageService.uploadFile(postKey, mockFile))
                .thenReturn(postUrl);

        // when
        String result = imageUploadService.uploadForSummernote(userId, mockFile);

        // then
        assertThat(result).isEqualTo(postUrl);

        // 이미지 개수 검증 확인
        verify(imageValidator).validateImageCount(userId, 1);

        // S3 업로드 검증
        verify(s3StorageService).uploadFile(postKey, mockFile);

        // DB 저장 검증 (postId는 null)
        verify(imageRepository).save(argThat(image ->
                image.getPath().equals(postKey) &&
                        image.getUrl().equals(postUrl) &&
                        image.getUserId().equals(userId) &&
                        image.getPostId() == null &&
                        image.getType() == ImageType.POST_CONTENT
        ));
    }

    @Test
    @DisplayName("게시글 이미지 삭제 성공 - 모든 이미지 삭제")
    void deletePostImages_success() {
        // given
        Long postId = 10L;
        Image image1 = new Image("key1", "url1", userId, postId, ImageType.POST_CONTENT, null);
        Image image2 = new Image("key2", "url2", userId, postId, ImageType.POST_CONTENT, null);

        when(imageRepository.findByPostId(postId))
                .thenReturn(Arrays.asList(image1, image2));

        // when
        imageUploadService.deletePostImages(postId);

        // then
        // 모든 이미지 S3에서 삭제 검증
        verify(s3StorageService).deleteFile("key1");
        verify(s3StorageService).deleteFile("key2");

        // 모든 이미지 DB에서 삭제 검증
        verify(imageRepository).delete(image1);
        verify(imageRepository).delete(image2);
    }

    @Test
    @DisplayName("게시글 이미지 삭제 - 이미지가 없는 경우")
    void deletePostImages_noImages() {
        // given
        Long postId = 10L;
        when(imageRepository.findByPostId(postId))
                .thenReturn(Collections.emptyList());

        // when
        imageUploadService.deletePostImages(postId);

        // then
        // S3 삭제 호출 안됨
        verify(s3StorageService, never()).deleteFile(anyString());

        // DB 삭제 호출 안됨
        verify(imageRepository, never()).delete(any(Image.class));
    }

    @Test
    @DisplayName("프로필 이미지 URL 조회 성공")
    void getProfileImageUrl_success() {
        // given
        Image profileImage = new Image(s3Key, s3Url, userId, null, ImageType.PROFILE, null);
        when(imageRepository.findByUserIdAndType(userId, ImageType.PROFILE))
                .thenReturn(List.of(profileImage));

        // when
        String result = imageUploadService.getProfileImageUrl(userId);

        // then
        assertThat(result).isEqualTo(s3Url);
    }

    @Test
    @DisplayName("프로필 이미지 URL 조회 - 이미지 없는 경우 null 반환")
    void getProfileImageUrl_notFound_returnsNull() {
        // given
        when(imageRepository.findByUserIdAndType(userId, ImageType.PROFILE))
                .thenReturn(Collections.emptyList());

        // when
        String result = imageUploadService.getProfileImageUrl(userId);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("게시글 이미지 리스트 조회 성공")
    void getPostImages_success() {
        // given
        Long postId = 10L;
        Image image1 = new Image("key1", "url1", userId, postId, ImageType.POST_CONTENT, null);
        Image image2 = new Image("key2", "url2", userId, postId, ImageType.POST_CONTENT, null);
        List<Image> expectedImages = Arrays.asList(image1, image2);

        when(imageRepository.findByPostId(postId))
                .thenReturn(expectedImages);

        // when
        List<Image> result = imageUploadService.getPostImages(postId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(image1, image2);
    }

    @Test
    @DisplayName("이미지를 게시글에 연결 성공")
    void linkImagesToPost_success() {
        // given
        Long postId = 10L;
        String url1 = "https://s3.amazonaws.com/bucket/key1";
        String url2 = "https://s3.amazonaws.com/bucket/key2";
        List<String> imageUrls = Arrays.asList(url1, url2);

        Image image1 = new Image("key1", url1, userId, null, ImageType.POST_CONTENT, null);
        Image image2 = new Image("key2", url2, userId, null, ImageType.POST_CONTENT, null);

        when(imageRepository.findByUserId(userId))
                .thenReturn(Arrays.asList(image1, image2));

        // when
        imageUploadService.linkImagesToPost(postId, userId, imageUrls);

        // then
        // setPostId()가 호출되었는지 검증하기 위해 이미지 객체의 상태 확인
        assertThat(image1.getPostId()).isEqualTo(postId);
        assertThat(image2.getPostId()).isEqualTo(postId);
    }

    @Test
    @DisplayName("이미지를 게시글에 연결 - 이미 postId가 있는 이미지는 업데이트하지 않음")
    void linkImagesToPost_skipsAlreadyLinkedImages() {
        // given
        Long postId = 10L;
        Long existingPostId = 5L;
        String url1 = "https://s3.amazonaws.com/bucket/key1";
        List<String> imageUrls = List.of(url1);

        // 이미 다른 게시글에 연결된 이미지
        Image linkedImage = new Image("key1", url1, userId, existingPostId, ImageType.POST_CONTENT, null);

        when(imageRepository.findByUserId(userId))
                .thenReturn(List.of(linkedImage));

        // when
        imageUploadService.linkImagesToPost(postId, userId, imageUrls);

        // then
        // 이미 연결된 이미지는 업데이트되지 않음
        assertThat(linkedImage.getPostId()).isEqualTo(existingPostId);
    }
}
