package com.toty.domain;

import com.toty.user.domain.vo.PhoneNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * PhoneNumber Value Object 단위 테스트
 * 목적: 010-XXXX-XXXX 형식 검증
 */
@ActiveProfiles("test")
@DisplayName("전화번호 Value Object 단위 테스트")
class PhoneNumberTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "010-1234-5678",
            "010-0000-0000",
            "010-9999-9999"
    })
    @DisplayName("유효한 전화번호 형식이면 PhoneNumber 객체가 생성된다")
    void createValidPhoneNumber(String validPhone) {
        // when
        PhoneNumber phoneNumber = PhoneNumber.of(validPhone);

        // then
        assertThat(phoneNumber).isNotNull();
        assertThat(phoneNumber.getValue()).isEqualTo(validPhone);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("전화번호가 null이거나 빈 문자열이면 예외가 발생한다")
    void createPhoneNumberWithNullOrEmpty(String invalidPhone) {
        // when & then
        assertThatThrownBy(() -> PhoneNumber.of(invalidPhone))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "01012345678",             // 하이픈 없음
            "010-12345-678",           // 잘못된 구분
            "010-123-5678",            // 중간 자리 부족
            "010-1234-567",            // 마지막 자리 부족
            "011-1234-5678",           // 010이 아님
            "010-abcd-efgh",           // 숫자 아님
            "010 1234 5678",           // 공백으로 구분
            "010.1234.5678"            // 점으로 구분
    })
    @DisplayName("잘못된 전화번호 형식이면 예외가 발생한다")
    void createPhoneNumberWithInvalidFormat(String invalidPhone) {
        // when & then
        assertThatThrownBy(() -> PhoneNumber.of(invalidPhone))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("전화번호가 13자를 초과하면 예외가 발생한다")
    void createPhoneNumberWithTooLongLength() {
        // given
        String longPhone = "010-1234-56789"; // 14자

        // when & then
        assertThatThrownBy(() -> PhoneNumber.of(longPhone))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("동일한 전화번호 값을 가진 PhoneNumber 객체는 동일하다")
    void phoneNumberEqualityByValue() {
        // given
        PhoneNumber phone1 = PhoneNumber.of("010-1234-5678");
        PhoneNumber phone2 = PhoneNumber.of("010-1234-5678");

        // when & then
        assertThat(phone1.getValue()).isEqualTo(phone2.getValue());
    }
}
