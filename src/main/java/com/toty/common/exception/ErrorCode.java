package com.toty.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 검색
    INVALID_SEARCH_FIELD(HttpStatus.BAD_REQUEST, "잘못된 검색 속성입니다."),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    USER_NOT_MENTOR(HttpStatus.FORBIDDEN, "해당 사용자는 멘토로 지정 되지 않았습니다."),
    PROFILE_IMAGE_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "프로필 이미지를 저장하는 중 오류가 발생했습니다."),
    Tag_Limit_Exceeded_Error(HttpStatus.BAD_REQUEST, "기술 태그는 10개 이하로 선정할 수 있습니다."),

    // 시간 형식
    INVALID_UTILITY_CLASS_INSTANTIATION(HttpStatus.INTERNAL_SERVER_ERROR, "유틸 클래스는 인스턴스화할 수 없습니다."),

    // 게시글
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // 게시글 태그 선택
    MISSING_REQUIRED_TAG(HttpStatus.BAD_REQUEST, "선택된 태그가 반드시 하나 필요 합니다."),
    TAG_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "태그 선택은 최대 5개만 가능 합니다."),

    // 게시글 카테고리
    CATEGORY_CREATION_STRATEGY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 카테고리에 대한 게시글 생성 전략이 존재하지 않습니다."),
    CATEGORY_UPDATE_STRATEGY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 카테고리에 대한 게시글 수정 전략이 존재하지 않습니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "잘못된 카테고리 값입니다."),

    // 댓글
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),

    // 팔로우
    CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "본인 계정은 팔로우할 수 없습니다."),

    // 알림
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),
    NOTIFICATIONS_DISABLED(HttpStatus.FORBIDDEN, "알림이 비활성화 되었습니다."),

    // 이메일
    EMAIL_CONSENT_DENIED(HttpStatus.FORBIDDEN, "이메일 수신 미동의 회원입니다."),
    EMAIL_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "이메일 미등록 사용자입니다."),
    MENTOR_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "멘토 템플릿을 찾을 수 없습니다."),
    REVOKE_MENTOR_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "멘토 해제 템플릿을 찾을 수 없습니다."),
    UNREAD_NOTIFICATION_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "읽지 않은 알림 템플릿을 찾을 수 없습니다."),
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "템플릿을 찾을 수 없습니다."),

    // sms
    SMS_CONSENT_DENIED(HttpStatus.FORBIDDEN, "문자 수신 미동의 회원입니다."),
    SMS_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "전화번호 미등록 사용자입니다."),

    // sse
    TOO_MANY_SSE_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "SSE 연결 요청이 너무 많습니다."),

    // FCM Token
    INVALID_FCM_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 FCM 토큰입니다."),
    FCM_TOKEN_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 토큰 저장에 실패했습니다."),
    FCM_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 FCM 토큰을 찾을 수 없습니다."),
    FCM_TOKEN_DEACTIVATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 토큰 비활성화에 실패했습니다."),
    FCM_TOKEN_QUERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 토큰 조회 중 오류가 발생했습니다."),
    FCM_TOKEN_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "비활성화된 FCM 토큰 삭제에 실패했습니다."),

    // 채팅
    FAIL_ROOM_CREATE(HttpStatus.FORBIDDEN, "단톡방을 생성할수 없습니다"),

    // 이미지
    IMAGE_COUNT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지 개수 제한을 초과했습니다."),
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 업로드에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}