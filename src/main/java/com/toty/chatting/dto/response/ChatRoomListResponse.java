package com.toty.chatting.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomListResponse {

    private long id;

    // 개설자 이름
    private String mentor;

    // 단톡방 이름
    private String roomName;

    // 참여 제한 인원
    private int userLimit;

    // 현재인원
    private int userCount;
    
    // 생성 시간
    private String createdAt;

}
