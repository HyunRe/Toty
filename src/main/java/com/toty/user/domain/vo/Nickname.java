package com.toty.user.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Nickname Value Object
 * - 닉네임 도메인 규칙을 캡슐화
 * - 불변 객체로 설계
 * - 한글, 영문, 숫자, 언더스코어, 하이픈만 허용
 * - 2~20자
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Nickname {
    private static final Pattern NICKNAME_PATTERN = Pattern.compile(
            "^[가-힣a-zA-Z0-9_-]+$"
    );
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 20;

    @Column(name = "nickname", nullable = false)
    private String value;

    private Nickname(String value) {
        validate(value);
        this.value = value;
    }

    public static Nickname of(String value) {
        return new Nickname(value);
    }

    private void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }

        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("닉네임은 %d자 이상 %d자 이하여야 합니다. 입력값: %s (%d자)",
                            MIN_LENGTH, MAX_LENGTH, value, value.length())
            );
        }

        if (!NICKNAME_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "닉네임은 한글, 알파벳, 숫자, 언더스코어, 하이픈만 포함 가능합니다. 입력값: " + value
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nickname nickname = (Nickname) o;
        return Objects.equals(value, nickname.value);
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
