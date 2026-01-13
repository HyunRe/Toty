package com.toty.chat.application.service;

import com.toty.chat.domain.model.ChatParticipant;
import com.toty.chat.domain.model.ChatRoom;
import com.toty.chat.domain.repository.ChatParticipantRepository;
import com.toty.chat.domain.repository.ChatRoomRepository;
import com.toty.chat.dto.response.ChatRoomListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅방 조회 전용 서비스 (CQRS 패턴)
 * - 채팅방 목록 조회
 * - DTO 변환 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomQueryService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    /**
     * 종료되지 않은 채팅방 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> getActiveChatRooms() {
        // N+1 문제 해결: fetch join 사용
        return chatRoomRepository.findAllByEndedAtWithMentor(null);
    }

    /**
     * 채팅방 목록을 Response DTO로 변환하여 반환
     */
    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getChatRoomListView() {
        List<ChatRoom> chatRooms = getActiveChatRooms();

        return chatRooms.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ChatRoom 엔티티를 ChatRoomListResponse DTO로 변환
     */
    private ChatRoomListResponse convertToResponse(ChatRoom chatRoom) {
        // 현재 참여자 수 조회
        List<ChatParticipant> currentParticipants =
                chatParticipantRepository.findAllByRoomAndExitAt(chatRoom, null);

        // 생성 시간 포맷팅
        LocalDateTime createdAt = chatRoom.getCreatedAt();
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH시mm분");
        String formattedCreatedAt = createdAt.format(formatter);

        return ChatRoomListResponse.builder()
                .id(chatRoom.getId())
                .mentor(chatRoom.getMentor().getNickname())
                .roomName(chatRoom.getRoomName())
                .createdAt(formattedCreatedAt)
                .userLimit(chatRoom.getUserLimit())
                .userCount(currentParticipants.size())
                .build();
    }
}
