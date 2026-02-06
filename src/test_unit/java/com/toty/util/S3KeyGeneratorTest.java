package com.toty.util;

import com.toty.common.image.domain.component.S3KeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * S3KeyGenerator 단위 테스트
 * 목적: S3 키 생성 로직 검증
 */
@ActiveProfiles("test")
@DisplayName("S3 키 생성기 유틸 단위 테스트")
class S3KeyGeneratorTest {

    private S3KeyGenerator s3KeyGenerator;

    @BeforeEach
    void setUp() {
        s3KeyGenerator = new S3KeyGenerator();
    }

    @Test
    @DisplayName("프로필 이미지 S3 키 생성 성공")
    void generateKey_profile() {
        // given
        String domain = "profile";
        Long id = 123L;
        String originalName = "test.jpg";

        // when
        String result = s3KeyGenerator.generateKey(domain, id, originalName);

        // then
        assertThat(result).startsWith("images/profile/123/");
        assertThat(result).endsWith(".jpg");
        assertThat(result).contains("images/profile/123/");
    }

    @Test
    @DisplayName("게시글 이미지 S3 키 생성 성공")
    void generateKey_post() {
        // given
        String domain = "post";
        Long id = 456L;
        String originalName = "image.png";

        // when
        String result = s3KeyGenerator.generateKey(domain, id, originalName);

        // then
        assertThat(result).startsWith("images/post/456/");
        assertThat(result).endsWith(".png");
        assertThat(result).contains("images/post/456/");
    }

    @Test
    @DisplayName("다양한 파일 확장자 처리")
    void generateKey_variousExtensions() {
        // given
        String[] extensions = {".jpg", ".png", ".gif", ".jpeg", ".webp"};

        for (String ext : extensions) {
            String originalName = "test" + ext;

            // when
            String result = s3KeyGenerator.generateKey("profile", 1L, originalName);

            // then
            assertThat(result).endsWith(ext);
        }
    }

    @Test
    @DisplayName("동일한 파일명으로 여러 번 생성 시 다른 UUID 생성")
    void generateKey_uniqueKeys() {
        // given
        String domain = "profile";
        Long id = 1L;
        String originalName = "test.jpg";

        // when
        String key1 = s3KeyGenerator.generateKey(domain, id, originalName);
        String key2 = s3KeyGenerator.generateKey(domain, id, originalName);
        String key3 = s3KeyGenerator.generateKey(domain, id, originalName);

        // then - UUID가 다르므로 모두 다른 키 생성
        assertThat(key1).isNotEqualTo(key2);
        assertThat(key2).isNotEqualTo(key3);
        assertThat(key1).isNotEqualTo(key3);
    }

    @Test
    @DisplayName("S3 키 형식 검증 - images/domain/id/uuid.ext")
    void generateKey_format() {
        // given
        String domain = "profile";
        Long id = 999L;
        String originalName = "photo.jpg";

        // when
        String result = s3KeyGenerator.generateKey(domain, id, originalName);

        // then
        String[] parts = result.split("/");
        assertThat(parts).hasSize(4);
        assertThat(parts[0]).isEqualTo("images");
        assertThat(parts[1]).isEqualTo("profile");
        assertThat(parts[2]).isEqualTo("999");
        assertThat(parts[3]).endsWith(".jpg");
    }

    @Test
    @DisplayName("복잡한 파일명 처리 - 마지막 확장자만 추출")
    void generateKey_complexFileName() {
        // given
        String domain = "post";
        Long id = 1L;
        String originalName = "my.photo.with.dots.jpg";

        // when
        String result = s3KeyGenerator.generateKey(domain, id, originalName);

        // then
        assertThat(result).endsWith(".jpg");
        assertThat(result).contains("images/post/1/");
    }

    @Test
    @DisplayName("다른 domain과 id 조합으로 다른 경로 생성")
    void generateKey_differentPaths() {
        // given
        String originalName = "test.jpg";

        // when
        String profileKey1 = s3KeyGenerator.generateKey("profile", 1L, originalName);
        String profileKey2 = s3KeyGenerator.generateKey("profile", 2L, originalName);
        String postKey1 = s3KeyGenerator.generateKey("post", 1L, originalName);

        // then
        assertThat(profileKey1).contains("images/profile/1/");
        assertThat(profileKey2).contains("images/profile/2/");
        assertThat(postKey1).contains("images/post/1/");
    }
}
