package com.toty.chat.application.service;

import com.toty.chat.domain.model.ChatParticipant;
import com.toty.chat.domain.model.ChatRoom;
import com.toty.chat.domain.repository.ChatParticipantRepository;
import com.toty.chat.domain.repository.ChatRoomRepository;
import com.toty.chat.dto.response.ChatRoomListResponse;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅방 서비스
 * - 채팅방 생성/종료 등 핵심 도메인 로직 담당
 * - 조회/메시징/알림 기능은 각각 전문 서비스에 위임
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatRoomMessagingService chatRoomMessagingService;
    private final ChatRoomNotificationService chatRoomNotificationService;

    /**
     * 멘토가 채팅방 종료
     * - 채팅방 종료 시간 기록
     * - 참가자들 퇴장 시간 기록
     * - WebSocket 및 Redis로 종료 알림 전송
     */
    @Transactional
    public void mentorEndRoom(Long userId, Long roomId) {
        if (roomId == null) {
            return;
        }

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        // 권한 검증: 멘토만 채팅방 종료 가능
        if (!userId.equals(chatRoom.getMentor().getId())) {
            throw new ExpectedException(ErrorCode.INSUFFICIENT_PERMISSION);
        }

        LocalDateTime endTime = LocalDateTime.now();

        // 채팅방 종료 시간 설정
        chatRoom.setEndedAt(endTime);
        chatRoomRepository.save(chatRoom);

        // 모든 참가자의 퇴장 시간 설정
        List<ChatParticipant> activeParticipants =
                chatParticipantRepository.findAllByRoomAndExitAt(chatRoom, null);
        activeParticipants.forEach(participant -> participant.setExitAt(endTime));
        chatParticipantRepository.saveAll(activeParticipants);

        // 메시징 서비스에 위임: WebSocket 및 Redis 메시지 전송
        chatRoomMessagingService.sendRoomClosedMessage(roomId);
        chatRoomMessagingService.publishRoomEndedEvent(roomId);

        log.info("채팅방 종료 완료 - roomId: {}, mentorId: {}", roomId, userId);
    }

    /**
     * 활성 채팅방 목록 조회 (QueryService에 위임)
     */
    public List<ChatRoom> getChatRoomList() {
        return chatRoomQueryService.getActiveChatRooms();
    }

    /**
     * 채팅방 목록 DTO 조회 (QueryService에 위임)
     */
    public List<ChatRoomListResponse> getChatRoomListView() {
        return chatRoomQueryService.getChatRoomListView();
    }

    /**
     * 멘토가 채팅방 생성
     * - 멘토 권한 검증
     * - 채팅방 생성 및 저장
     * - 메시징 서비스에 위임: 생성 이벤트 발행
     * - 알림 서비스에 위임: 팔로워들에게 알림 전송
     */
    @Transactional
    public void mentorCreateRoom(long userId, String roomName, int userLimit) {
        User mentor = userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        // 멘토 권한 검증
        if (!"MENTOR".equals(mentor.getRole().name())) {
            throw new ExpectedException(ErrorCode.FAIL_ROOM_CREATE);
        }

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .mentor(mentor)
                .roomName(roomName)
                .userLimit(userLimit)
                .build();
        ChatRoom createdRoom = chatRoomRepository.save(chatRoom);

        log.info("채팅방 생성 완료 - roomId: {}, mentorId: {}, roomName: {}",
                createdRoom.getId(), userId, roomName);

        // 메시징 서비스에 위임: Redis Pub/Sub로 채팅방 생성 알림
        // ChatRoomListResponse 직접 생성
        List<ChatParticipant> currentParticipants =
                chatParticipantRepository.findAllByRoomAndExitAt(createdRoom, null);

        ChatRoomListResponse chatRoomResponse = ChatRoomListResponse.builder()
                .id(createdRoom.getId())
                .mentor(createdRoom.getMentor().getNickname())
                .roomName(createdRoom.getRoomName())
                .createdAt(createdRoom.getCreatedAt() != null ?
                        createdRoom.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("HH시mm분")) :
                        LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH시mm분")))
                .userLimit(createdRoom.getUserLimit())
                .userCount(currentParticipants.size())
                .build();

        chatRoomMessagingService.publishRoomCreatedEvent(chatRoomResponse);

        // 알림 서비스에 위임: 팔로워들에게 알림 전송
        chatRoomNotificationService.notifyFollowersAboutNewChatRoom(mentor, createdRoom.getId());
    }


}
