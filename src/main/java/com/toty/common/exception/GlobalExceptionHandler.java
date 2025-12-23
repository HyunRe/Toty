package com.toty.common.exception;

import com.toty.common.response.TotyResponse;
<<<<<<< HEAD
import jakarta.servlet.http.HttpServletRequest;
=======
>>>>>>> origin/develop
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
<<<<<<< HEAD
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.IOException;
=======
>>>>>>> origin/develop

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
<<<<<<< HEAD
    @ExceptionHandler(ExpectedException.class)
    public ResponseEntity<?> handleExpectedException(ExpectedException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();

        // SSE 요청인 경우 JSON 응답 대신 빈 응답 반환
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("text/event-stream")) {
            log.warn("SSE 요청 중 예외 발생: {}, errorCode: {}", ex.getMessage(), errorCode);
            // SSE 요청에서는 JSON을 반환할 수 없으므로 NO_CONTENT 반환
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
=======

    @ExceptionHandler(ExpectedException.class)
    public ResponseEntity<TotyResponse> handleExpectedException(ExpectedException ex) {
        ErrorCode errorCode = ex.getErrorCode();
>>>>>>> origin/develop

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 응답 헤더에는 400 코드가 가되,
                .body(TotyResponse.error(errorCode)); // 실제 상태 코드는 ErrorCode의 상태 코드이다.
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
<<<<<<< HEAD
    public ResponseEntity<TotyResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
=======
    public ResponseEntity<TotyResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
>>>>>>> origin/develop
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

<<<<<<< HEAD
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<TotyResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("잘못된 인자 예외: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(TotyResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<TotyResponse<Void>> handleUnExpectedException(Exception ex) {
=======
    @ExceptionHandler(Exception.class)
    public ResponseEntity<TotyResponse> handleUnExpectedException(Exception ex) {
>>>>>>> origin/develop
        log.error("에러 발생 :", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TotyResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."));
    }
<<<<<<< HEAD

    /**
     * SSE Broken pipe 에러 무시
     * - 클라이언트가 연결을 끊은 경우 정상적인 동작이므로 로그만 남기고 무시
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Void> handleIOException(IOException e) {
        if (e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
            log.debug("SSE 연결 끊김 (정상): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        log.error("IO 에러 발생:", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * SSE AsyncRequestNotUsableException 무시
     * - 클라이언트가 이미 연결을 끊은 후 응답 시도 시 발생
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public ResponseEntity<Void> handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        log.debug("SSE 비동기 요청 사용 불가 (정상): {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
=======
>>>>>>> origin/develop
}