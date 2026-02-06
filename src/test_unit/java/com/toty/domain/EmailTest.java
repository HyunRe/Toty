package com.toty.domain;

import com.toty.user.domain.vo.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * Email Value Object 단위 테스트
 * 목적: 도메인 규칙을 코드 레벨에서 보장
 */
@ActiveProfiles("test")
@DisplayName("이메일 Value Object 단위 테스트")
class EmailTest {

    @Test
    @DisplayName("유효한 이메일 형식이면 Email 객체가 생성된다")
    void createValidEmail() {
        // given
        String validEmail = "test@example.com";

        // when
        Email email = Email.of(validEmail);

        // then
        assertThat(email).isNotNull();
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("이메일은 소문자로 정규화된다")
    void emailNormalizedToLowerCase() {
        // given
        String upperCaseEmail = "TEST@EXAMPLE.COM";

        // when
        Email email = Email.of(upperCaseEmail);

        // then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("이메일이 null이거나 빈 문자열이면 예외가 발생한다")
    void createEmailWithNullOrEmpty(String invalidEmail) {
        // when & then
        assertThatThrownBy(() -> Email.of(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일은 필수입니다");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid-email",           // @ 없음
            "@example.com",            // 로컬 파트 없음
            "test@",                   // 도메인 없음
            "test@@example.com",       // @ 중복
            "test@example",            // TLD 없음
            "test @example.com",       // 공백 포함
            "test@ex ample.com"        // 도메인에 공백
    })
    @DisplayName("잘못된 이메일 형식이면 예외가 발생한다")
    void createEmailWithInvalidFormat(String invalidEmail) {
        // when & then
        assertThatThrownBy(() -> Email.of(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 이메일 형식입니다");
    }

    @Test
    @DisplayName("이메일 길이가 255자를 초과하면 예외가 발생한다")
    void createEmailWithTooLongLength() {
        // given
        String longEmail = "a".repeat(250) + "@example.com"; // 256자

        // when & then
        assertThatThrownBy(() -> Email.of(longEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("255자를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("정확히 255자의 이메일은 생성된다")
    void createEmailWithExactly255Characters() {
        // given
        String domain = "@example.com";
        int maxLength = 255;
        String email255 = "a".repeat(maxLength - domain.length()) + domain;

        // when
        Email email = Email.of(email255);

        // then
        assertThat(email).isNotNull();
        assertThat(email.getValue()).hasSize(255);
    }

    @Test
    @DisplayName("동일한 이메일 값을 가진 Email 객체는 동일하다")
    void emailEqualityByValue() {
        // given
        Email email1 = Email.of("test@example.com");
        Email email2 = Email.of("TEST@EXAMPLE.COM");

        // when & then
        assertThat(email1.getValue()).isEqualTo(email2.getValue());
    }
}
