package com.toty.user.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * PhoneNumber Value Object
 * - 휴대폰 번호 도메인 규칙을 캡슐화
 * - 불변 객체로 설계
 * - 형식: 010-XXXX-XXXX
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneNumber {
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^010-\\d{4}-\\d{4}$"
    );

    @Column(name = "phone_number")
    private String value;

    private PhoneNumber(String value) {
        validate(value);
        this.value = value;
    }

    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }

    private void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("휴대폰 번호는 필수입니다.");
        }

        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "유효하지 않은 휴대폰 번호 형식입니다. 형식: 010-XXXX-XXXX, 입력값: " + value
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
