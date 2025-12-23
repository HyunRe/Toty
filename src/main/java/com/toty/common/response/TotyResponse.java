package com.toty.common.response;

import com.toty.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TotyResponse<T> {
    private final int status;
    private final T data;
    private final String errorMessage;

    // 성공 응답 - 200 상태코드의 경우 상태코드를 파라미터로 전달 받지 않음
    // 개발 시 Controller 응답에 직접 사용
    public static <T> TotyResponse<T> success(T data) {
        return new TotyResponse<>(HttpStatus.OK.value(), data, null);
    }

    // 성공 응답 - 200이 아닌 상태코드의 경우 상태코드를 파라미터로 전달 받음
    // 개발 시 Controller 응답에 직접 사용
    public static <T> TotyResponse<T> success(T data, HttpStatus status) {
        return new TotyResponse<>(status.value(), data, null);
    }

    // 실패 응답
    // GlobalExceptionHandler에서 사용됨
    public static <T> TotyResponse<T> error(ErrorCode errorCode) {
        return new TotyResponse<>(errorCode.getHttpStatus().value(), null, errorCode.getMessage());
    }

    // 실패 응답 - 에러 메세지가 정해져 있지 않은 경우
    // GlobalExceptionHandler에서 사용됨
    public static <T> TotyResponse<T> error(HttpStatus httpStatus, String errorMessage) {
        return new TotyResponse<>(httpStatus.value(), null, errorMessage);
    }
}