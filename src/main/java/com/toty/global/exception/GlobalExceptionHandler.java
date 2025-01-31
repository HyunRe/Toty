package com.toty.global.exception;

import com.toty.global.response.TotyResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExpectedException.class)
    public ResponseEntity<TotyResponse> handleExpectedException(ExpectedException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 응답 헤더에는 400 코드가 가되,
                .body(TotyResponse.error(errorCode)); // 실제 상태 코드는 ErrorCode의 상태 코드이다.
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<TotyResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder builder = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            builder.append("[");
            builder.append(fieldError.getField());
            builder.append("](은)는 ");
            builder.append(fieldError.getDefaultMessage());
            builder.append(" 입력된 값: [");
            builder.append(fieldError.getRejectedValue());
            builder.append("]");
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(TotyResponse.error(HttpStatus.BAD_REQUEST, builder.toString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<TotyResponse> handleUnExpectedException(Exception ex) {
        // 추후 ex.getMessage() 로깅 추가 필요

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TotyResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."));
    }
}