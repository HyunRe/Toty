package com.toty.chat.domain.repository;

import com.toty.chat.domain.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // N+1 문제 해결을 위한 fetch join 쿼리
    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
           "LEFT JOIN FETCH cr.mentor " +
           "WHERE cr.endedAt = :endedAt")
    List<ChatRoom> findAllByEndedAtWithMentor(@Param("endedAt") LocalDateTime endedAt);

    // 기존 메서드 (하위 호환성 유지)
    List<ChatRoom> findAllByEndedAt(LocalDateTime endedAt);
}
