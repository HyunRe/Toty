package com.toty.chatting.domain.repository;

import com.toty.chatting.domain.model.ChatParticipant;
import com.toty.chatting.domain.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    Optional<ChatParticipant> findByChatterAndRoom(User01 chatter, ChatRoom room);
    Optional<ChatParticipant> findByChatterAndRoomAndExitAt(User01 chatter, ChatRoom room, LocalDateTime exitTime);

    Optional<ChatParticipant> findByChatter(User01 chatter);

    List<ChatParticipant> findAllByRoomAndExitAt(ChatRoom room, LocalDateTime exitTime);
    // 단톡방 현재 참석자들 가져오는거 : exitTime = null
}
