package com.toty.chatting.domain.repository;

import com.toty.chatting.domain.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
