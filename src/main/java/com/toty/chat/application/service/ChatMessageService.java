package com.toty.chat.application.service;

import com.toty.chat.domain.model.ChatMessage;
import com.toty.chat.domain.model.ChatParticipant;
import com.toty.chat.domain.model.ChatRoom;
import com.toty.chat.domain.repository.ChatMessageRepository;
import com.toty.chat.domain.repository.ChatParticipantRepository;
import com.toty.chat.domain.repository.ChatRoomRepository;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatRoomRepository chatRoom02Repository;
    private final ChatParticipantRepository chatParticipant02Repository;
    private final ChatMessageRepository chatMessage02Repository;
    private final UserRepository userRepository;

    /*
        채팅메시지 저장하기
     */
    public void saveMessage(long roomId, long senderId, String message) {
        User user01 = userRepository.findById(senderId).orElse(null);
        ChatRoom chatRoom = chatRoom02Repository.findById(roomId).orElse(null);
        ChatParticipant chatParticipant02 = chatParticipant02Repository.findByChatterAndRoomAndExitAt(user01, chatRoom, null).orElse(null);

        if (chatParticipant02 != null && chatRoom != null) {
            ChatMessage dbChatMessage = ChatMessage.builder()
                    .room(chatRoom).sender(chatParticipant02).message(message)
                    .build();
            chatMessage02Repository.save(dbChatMessage);
        }
    }

}
