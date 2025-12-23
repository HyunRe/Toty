package com.toty.chat.domain.repository;

import com.toty.chat.domain.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findAllByEndedAt(LocalDateTime endedAt);
}
