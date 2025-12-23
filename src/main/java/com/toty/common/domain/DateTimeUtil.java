package com.toty.common.domain;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateTimeUtil() {
        throw new ExpectedException(ErrorCode.INVALID_UTILITY_CLASS_INSTANTIATION);
    }

    // 상대 시간
    public static String formatRelativeTime(LocalDateTime createdAt) {
        if (createdAt == null) return null;

        long seconds = Duration.between(createdAt, LocalDateTime.now()).getSeconds();

        if (seconds < 60) return "방금 전";
        if (seconds < 3600) return seconds / 60 + "분 전";
        if (seconds < 86400) return seconds / 3600 + "시간 전";
        if (seconds < 172800) return "어제";
        if (seconds < 2592000) return seconds / 86400 + "일 전"; // 30일 미만 → 일 단위
        if (seconds < 31536000) return seconds / 2592000 + "달 전"; // 12개월 미만 → 달 단위

        return seconds / 31536000 + "년 전"; // 1년 이상
    }

    // 절대 시간
    public static String formatDate(LocalDateTime createdAt) {
        if (createdAt == null) return null;
        return createdAt.format(DATE_FORMATTER);
    }
}
