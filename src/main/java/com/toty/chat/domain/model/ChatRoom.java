package com.toty.chat.domain.model;

import com.toty.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // 단톡방 개설자
    @ManyToOne
    @JoinColumn(name = "mento_id")
    private User mentor;

    // 단톡방 이름
    private String roomName;

    // 참여 제한 인원
    private int userLimit;

    // 생성 시간
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false)
    private LocalDateTime createdAt; // insertable = false 안해주면 db에 null로 들어감, 아마 jpa가 필드 값을 명시적으로 설정하지 않으면 null로 처리해서 그런듯

    // 종료 시간
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime endedAt;
}
