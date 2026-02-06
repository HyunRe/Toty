package com.toty.util;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.common.image.domain.component.ImageValidator;
import com.toty.common.image.domain.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ImageValidator 단위 테스트
 * 목적: 이미지 개수 제한 검증 (사용자당 최대 10개)
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("이미지 검증 유틸 단위 테스트")
class ImageValidatorTest {

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageValidator imageValidator;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @Test
    @DisplayName("이미지 개수 검증 성공 - 제한 이내")
    void validateImageCount_success() {
        // given
        when(imageRepository.countByUserId(userId)).thenReturn(5L);

        // when & then - 예외 발생하지 않음
        assertThatCode(() -> imageValidator.validateImageCount(userId, 1))
                .doesNotThrowAnyException();

        verify(imageRepository).countByUserId(userId);
    }

    @Test
    @DisplayName("이미지 개수 검증 성공 - 정확히 10개")
    void validateImageCount_success_exactly10() {
        // given
        when(imageRepository.countByUserId(userId)).thenReturn(9L);

        // when & then - 1개 추가로 정확히 10개 (성공)
        assertThatCode(() -> imageValidator.validateImageCount(userId, 1))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미지 개수 검증 실패 - 10개 초과")
    void validateImageCount_failure_exceeds10() {
        // given
        when(imageRepository.countByUserId(userId)).thenReturn(10L);

        // when & then - 1개 추가 시도 시 예외 발생
        assertThatThrownBy(() -> imageValidator.validateImageCount(userId, 1))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_COUNT_LIMIT_EXCEEDED);

        verify(imageRepository).countByUserId(userId);
    }

    @Test
    @DisplayName("이미지 개수 검증 실패 - 여러 개 업로드로 초과")
    void validateImageCount_failure_multipleUploads() {
        // given
        when(imageRepository.countByUserId(userId)).thenReturn(8L);

        // when & then - 3개 추가 시도 시 11개가 되므로 예외 발생
        assertThatThrownBy(() -> imageValidator.validateImageCount(userId, 3))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_COUNT_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("이미지 개수 검증 성공 - 기존 이미지 없음")
    void validateImageCount_success_noExistingImages() {
        // given
        when(imageRepository.countByUserId(userId)).thenReturn(0L);

        // when & then - 5개 추가 (성공)
        assertThatCode(() -> imageValidator.validateImageCount(userId, 5))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미지 개수 검증 실패 - 이미 10개 있음")
    void validateImageCount_failure_already10() {
        // given
        when(imageRepository.countByUserId(userId)).thenReturn(10L);

        // when & then - 추가 업로드 불가
        assertThatThrownBy(() -> imageValidator.validateImageCount(userId, 1))
                .isInstanceOf(ExpectedException.class);
    }

    @Test
    @DisplayName("이미지 개수 검증 - 경계값 테스트 (9개 있을 때 2개 추가)")
    void validateImageCount_boundary_9plus2() {
        // given
        when(imageRepository.countByUserId(userId)).thenReturn(9L);

        // when & then - 11개가 되므로 예외 발생
        assertThatThrownBy(() -> imageValidator.validateImageCount(userId, 2))
                .isInstanceOf(ExpectedException.class);
    }

    @Test
    @DisplayName("여러 사용자가 각각 이미지 업로드 가능")
    void validateImageCount_multipleUsers() {
        // given
        Long user1 = 1L;
        Long user2 = 2L;

        when(imageRepository.countByUserId(user1)).thenReturn(5L);
        when(imageRepository.countByUserId(user2)).thenReturn(8L);

        // when & then - 각 사용자 독립적으로 검증
        assertThatCode(() -> imageValidator.validateImageCount(user1, 1))
                .doesNotThrowAnyException();
        assertThatCode(() -> imageValidator.validateImageCount(user2, 1))
                .doesNotThrowAnyException();

        verify(imageRepository).countByUserId(user1);
        verify(imageRepository).countByUserId(user2);
    }
}
