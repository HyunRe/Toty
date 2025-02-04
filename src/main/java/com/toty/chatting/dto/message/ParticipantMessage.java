package com.toty.chatting.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantMessage {
    private long chatterId;
    private String chatterName;
    private int access; // 1: 입장,  0: 퇴장
}
