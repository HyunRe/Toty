package com.toty.user.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Email Value Object
 * - 이메일 도메인 규칙을 캡슐화
 * - 불변 객체로 설계
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Column(name = "email", nullable = false, unique = true)
    private String value;

    private Email(String value) {
        validate(value);
        this.value = value.toLowerCase(); // 이메일은 소문자로 정규화
    }

    public static Email of(String value) {
        return new Email(value);
    }

    private void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다: " + value);
        }

        if (value.length() > 255) {
            throw new IllegalArgumentException("이메일은 255자를 초과할 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
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
