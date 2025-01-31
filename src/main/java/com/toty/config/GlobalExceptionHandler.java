package com.toty.config;

import com.toty.base.exception.BaseException;
import com.toty.base.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleBaseException(BaseException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getStatus().value(), // 상태 코드
                ex.getMessage(),        // 에러 메시지
                null                    // 추가 에러 리스트는 null 처리
        );

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException .class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        // 유효성 검증 실패한 필드별 에러 메시지 추출
        List<String> errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> String.format("필드 '%s'는 %s", fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "잘못된 요청 입니다.",
                errorMessages
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}

