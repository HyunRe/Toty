package com.toty.chatting.presentation;


import com.toty.chatting.application.ChatMessageService;
import com.toty.chatting.dto.message.RecieveMessage;
import com.toty.chatting.dto.message.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@Slf4j
public class ChattingStompController {
    @Autowired private ChatMessageService chatMessageService;

    /*
        @MessageMapping 메서드에서는 일반적인 HTTP기반의 HttpSession을 직접 주입받을 수 없다.
     */
    @MessageMapping("/{roomId}")
    @SendTo("/chatRoom/{roomId}/message")
    public RecieveMessage hello(SendMessage sendMessage
                                , @DestinationVariable("roomId") long roomId) {

        long senderId = sendMessage.getSenderId();
        String sender = sendMessage.getSender();
        String message = sendMessage.getMessage();

        chatMessageService.saveMessage(roomId, senderId, message);

        RecieveMessage msg = RecieveMessage.builder()
                .content(message).sender(sender).sendedAt(LocalDateTime.now())
                .build();

        return msg;
    }
}
