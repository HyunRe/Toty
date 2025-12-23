package com.toty.chat.presentation.stomp;

import com.toty.chat.application.service.ChatMessageService;
import com.toty.chat.dto.message.RecieveMessage;
import com.toty.chat.dto.message.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatStompController {
    private final ChatMessageService chatMessageService;

    /*
        @MessageMapping 메서드에서는 일반적인 HTTP기반의 HttpSession을 직접 주입받을 수 없다.
     */
    @MessageMapping("/{roomId}")
    @SendTo("/chatRoom/{roomId}/message")
    public RecieveMessage hello(SendMessage sendMessage,
                                @DestinationVariable("roomId") long roomId) {
        long senderId = sendMessage.getSenderId();
        String sender = sendMessage.getSender();
        String message = sendMessage.getMessage();

        chatMessageService.saveMessage(roomId, senderId, message);

        // 날짜 String타입의 원하는 형식으로 변형
        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH시mm분");
        String formattedSendedAt = ldt.format(formatter);

        RecieveMessage msg = RecieveMessage.builder()
                .content(message).sender(sender).sendedAt(formattedSendedAt)
                .build();

        return msg;
    }
}
