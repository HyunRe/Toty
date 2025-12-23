package com.toty.chat.domain.repository;

import com.toty.chat.domain.model.ChatParticipant;
import com.toty.chat.domain.model.ChatRoom;
import com.toty.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    Optional<ChatParticipant> findByChatterAndRoom(User chatter, ChatRoom room);
    Optional<ChatParticipant> findByChatterAndRoomAndExitAt(User chatter, ChatRoom room, LocalDateTime exitTime);

    Optional<ChatParticipant> findByChatter(User chatter);

    List<ChatParticipant> findAllByRoomAndExitAt(ChatRoom room, LocalDateTime exitTime);
    // 단톡방 현재 참석자들 가져오는거 : exitTime = null
}
