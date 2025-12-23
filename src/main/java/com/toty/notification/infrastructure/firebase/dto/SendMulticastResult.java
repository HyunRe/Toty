package com.toty.notification.infrastructure.firebase.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SendMulticastResult {
    private int requestedCount;               // 요청한 총 토큰 수
    private int successCount;                 // 성공 개수
    private int failureCount;                 // 실패 개수
    private List<String> invalidTokens;       // 만료/무효 토큰 (정리 권장)
    private Map<String, String> tokenErrors;  // token -> error message

    public SendMulticastResult(int requestedCount, int successCount, int failureCount, List<String> invalidTokens, Map<String, String> tokenErrors) {
        this.requestedCount = requestedCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.invalidTokens = invalidTokens;
        this.tokenErrors = tokenErrors;
    }
}
