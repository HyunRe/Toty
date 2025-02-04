package com.toty.chatting.presentation;


import com.toty.chatting.application.ChatMessageService;
import com.toty.chatting.dto.RecieveMessage02DTO;
import com.toty.chatting.dto.SendMessage02DTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@Slf4j
public class Stomp02Controller {
    @Autowired private ChatMessageService chatMessageService;

    /*
        @MessageMapping 메서드에서는 일반적인 HTTP기반의 HttpSession을 직접 주입받을 수 없다.
     */
    @MessageMapping("/{roomId}")
    @SendTo("/chatRoom/{roomId}/message")
    public RecieveMessage02DTO hello(SendMessage02DTO sendMessage02DTO
                                , @DestinationVariable("roomId") long roomId) {

        long senderId = sendMessage02DTO.getSenderId();
        String sender = sendMessage02DTO.getSender();
        String message = sendMessage02DTO.getMessage();

        chatMessageService.saveMessage(roomId, senderId, message);

        RecieveMessage02DTO msg = RecieveMessage02DTO.builder()
                .content(message).sender(sender).sendedAt(LocalDateTime.now())
                .build();

        return msg;
    }
}
