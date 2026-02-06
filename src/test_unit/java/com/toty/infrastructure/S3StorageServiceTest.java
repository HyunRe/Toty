package com.toty.infrastructure;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.common.image.infrastructure.S3StorageService;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * S3StorageService 단위 테스트
 * 목적: S3 파일 업로드/삭제 로직을 Mock으로 검증
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("S3 스토리지 서비스 인프라 테스트")
class S3StorageServiceTest {

    @Mock
    private S3Template s3Template;

    @InjectMocks
    private S3StorageService s3StorageService;

    @Mock
    private MultipartFile mockFile;

    @Mock
    private S3Resource mockS3Resource;

    private String bucket;
    private String s3Key;

    @BeforeEach
    void setUp() {
        bucket = "test-bucket";
        s3Key = "images/profile/1/test.jpg";

        // @Value로 주입되는 bucket 값 설정
        ReflectionTestUtils.setField(s3StorageService, "bucket", bucket);
    }

    @Test
    @DisplayName("S3 파일 업로드 성공")
    void uploadFile_success() throws Exception {
        // given
        byte[] fileContent = "test image content".getBytes();
        String expectedUrl = "https://s3.amazonaws.com/" + bucket + "/" + s3Key;

        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));
        when(s3Template.upload(eq(bucket), eq(s3Key), any()))
                .thenReturn(mockS3Resource);
        when(mockS3Resource.getURL()).thenReturn(new URL(expectedUrl));

        // when
        String result = s3StorageService.uploadFile(s3Key, mockFile);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        verify(s3Template).upload(eq(bucket), eq(s3Key), any());
    }

    @Test
    @DisplayName("S3 파일 업로드 실패 - IOException 발생 시 ExpectedException")
    void uploadFile_failure_throwsExpectedException() throws Exception {
        // given
        when(mockFile.getInputStream()).thenThrow(new IOException("파일 읽기 실패"));

        // when & then
        assertThatThrownBy(() -> s3StorageService.uploadFile(s3Key, mockFile))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.S3_UPLOAD_FAILED);

        verify(s3Template, never()).upload(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("S3 파일 업로드 실패 - S3Template 업로드 실패")
    void uploadFile_failure_s3TemplateError() throws Exception {
        // given
        byte[] fileContent = "test image content".getBytes();
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));
        when(s3Template.upload(eq(bucket), eq(s3Key), any()))
                .thenThrow(new RuntimeException("S3 업로드 실패"));

        // when & then
        assertThatThrownBy(() -> s3StorageService.uploadFile(s3Key, mockFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("S3 업로드 실패");
    }

    @Test
    @DisplayName("S3 파일 비동기 삭제 성공")
    void deleteFile_success() {
        // given
        doNothing().when(s3Template).deleteObject(bucket, s3Key);

        // when
        s3StorageService.deleteFile(s3Key);

        // then
        verify(s3Template).deleteObject(bucket, s3Key);
    }

    @Test
    @DisplayName("S3 파일 비동기 삭제 실패 - 예외 발생해도 메서드는 정상 종료")
    void deleteFile_failure_doesNotThrow() {
        // given
        doThrow(new RuntimeException("S3 삭제 실패"))
                .when(s3Template).deleteObject(bucket, s3Key);

        // when & then - 예외가 발생하지 않아야 함 (로그만 남김)
        assertThatCode(() -> s3StorageService.deleteFile(s3Key))
                .doesNotThrowAnyException();

        verify(s3Template).deleteObject(bucket, s3Key);
    }

    @Test
    @DisplayName("다양한 파일 형식 업로드 성공")
    void uploadFile_variousFormats() throws Exception {
        // given
        String[] keys = {
                "images/profile/1/test.jpg",
                "images/profile/2/test.png",
                "images/post/3/test.gif",
                "images/post/4/test.webp"
        };

        for (String key : keys) {
            byte[] fileContent = "test content".getBytes();
            String expectedUrl = "https://s3.amazonaws.com/" + bucket + "/" + key;

            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));
            when(s3Template.upload(eq(bucket), eq(key), any()))
                    .thenReturn(mockS3Resource);
            when(mockS3Resource.getURL()).thenReturn(new URL(expectedUrl));

            // when
            String result = s3StorageService.uploadFile(key, mockFile);

            // then
            assertThat(result).isEqualTo(expectedUrl);
        }

        verify(s3Template, times(4)).upload(eq(bucket), anyString(), any());
    }

    @Test
    @DisplayName("여러 파일 동시 삭제")
    void deleteMultipleFiles() {
        // given
        String[] keys = {
                "images/profile/1/old1.jpg",
                "images/profile/1/old2.jpg",
                "images/post/5/old3.jpg"
        };

        doNothing().when(s3Template).deleteObject(eq(bucket), anyString());

        // when
        for (String key : keys) {
            s3StorageService.deleteFile(key);
        }

        // then
        verify(s3Template, times(3)).deleteObject(eq(bucket), anyString());
    }
}
