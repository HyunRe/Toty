package com.toty.chatting.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessage {
    private String sender;
    private long senderId;
    private String message;
}
