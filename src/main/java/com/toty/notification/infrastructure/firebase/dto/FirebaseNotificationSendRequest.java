package com.toty.notification.infrastructure.firebase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FirebaseNotificationSendRequest {
    @NotNull(message = "수신자 닉네임은 필수입니다.")
    private String senderNickname;

    @NotNull(message = "제목은 필수입니다.")
    @Size(min = 1, max = 30, message = "제목은 1자 이상, 15 이하이어야 합니다.")
    private String title;

    @NotNull(message = "메시지는 필수입니다.")
    @Size(min = 1, max = 25, message = "메시지는 1자 이상, 30 이하이어야 합니다.")
    private String message;

    @NotNull(message = "URL은 필수입니다.")
    @Size(min = 1, max = 100, message = "URL은 1자 이상, 100 이하이어야 합니다.")
    private String url;

    @Schema(description = "추가 데이터(읽음 여부, 라우팅 데이터 등)")
    private Map<String, String> extraData;

    public FirebaseNotificationSendRequest(String senderNickname, String message, String url) {
        this.senderNickname = senderNickname;
        this.message = message;
        this.url = url;
    }
}
