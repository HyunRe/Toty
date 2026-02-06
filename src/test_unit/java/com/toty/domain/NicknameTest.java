package com.toty.domain;

import com.toty.user.domain.vo.Nickname;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * Nickname Value Object 단위 테스트
 * 목적: 한글/영문/숫자/특수문자 2~20자 검증
 */
@ActiveProfiles("test")
@DisplayName("닉네임 Value Object 단위 테스트")
class NicknameTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "테스트",                    // 한글 2자
            "Test",                    // 영문 4자
            "테스트User",               // 한글+영문
            "테스트123",                // 한글+숫자
            "Test_User",               // 영문+특수문자
            "닉네임_123",               // 한글+숫자+특수문자
            "AAAAAAAAAAAAAAAAAAAA"     // 최대 20자
    })
    @DisplayName("유효한 닉네임 형식이면 Nickname 객체가 생성된다")
    void createValidNickname(String validNickname) {
        // when
        Nickname nickname = Nickname.of(validNickname);

        // then
        assertThat(nickname).isNotNull();
        assertThat(nickname.getValue()).isEqualTo(validNickname);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("닉네임이 null이거나 빈 문자열이면 예외가 발생한다")
    void createNicknameWithNullOrEmpty(String invalidNickname) {
        // when & then
        assertThatThrownBy(() -> Nickname.of(invalidNickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("닉네임은 필수입니다");
    }

    @Test
    @DisplayName("닉네임이 2자 미만이면 예외가 발생한다")
    void createNicknameWithTooShortLength() {
        // given
        String shortNickname = "A";

        // when & then
        assertThatThrownBy(() -> Nickname.of(shortNickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("닉네임은 2자 이상 20자 이하여야 합니다");
    }

    @Test
    @DisplayName("닉네임이 20자를 초과하면 예외가 발생한다")
    void createNicknameWithTooLongLength() {
        // given
        String longNickname = "A".repeat(21);

        // when & then
        assertThatThrownBy(() -> Nickname.of(longNickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("닉네임은 2자 이상 20자 이하여야 합니다");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "닉네임@",                   // 허용되지 않는 특수문자
            "Test#User",               // 허용되지 않는 특수문자
            "닉네임!",                   // 허용되지 않는 특수문자
            "Test User",               // 공백
            "닉네임\n",                  // 개행문자
            "닉네임\t"                   // 탭문자
    })
    @DisplayName("허용되지 않는 문자가 포함되면 예외가 발생한다")
    void createNicknameWithInvalidCharacters(String invalidNickname) {
        // when & then
        assertThatThrownBy(() -> Nickname.of(invalidNickname))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("정확히 2자의 닉네임은 생성된다")
    void createNicknameWithExactly2Characters() {
        // given
        String nickname = "AB";

        // when
        Nickname result = Nickname.of(nickname);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getValue()).hasSize(2);
    }

    @Test
    @DisplayName("정확히 20자의 닉네임은 생성된다")
    void createNicknameWithExactly20Characters() {
        // given
        String nickname = "A".repeat(20);

        // when
        Nickname result = Nickname.of(nickname);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getValue()).hasSize(20);
    }

    @Test
    @DisplayName("동일한 닉네임 값을 가진 Nickname 객체는 동일하다")
    void nicknameEqualityByValue() {
        // given
        Nickname nickname1 = Nickname.of("테스트");
        Nickname nickname2 = Nickname.of("테스트");

        // when & then
        assertThat(nickname1.getValue()).isEqualTo(nickname2.getValue());
    }
}
