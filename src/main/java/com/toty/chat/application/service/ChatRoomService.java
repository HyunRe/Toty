package com.toty.chat.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toty.chat.domain.model.ChatParticipant;
import com.toty.chat.domain.model.ChatRoom;
import com.toty.chat.domain.repository.ChatParticipantRepository;
import com.toty.chat.domain.repository.ChatRoomRepository;
import com.toty.chat.dto.response.ChatRoomListResponse;
import com.toty.common.baseException.JsonProcessingCustomException;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.following.domain.model.Following;
import com.toty.following.domain.repository.FollowingRepository;
import com.toty.notification.application.service.NotificationSendService;
import com.toty.notification.domain.type.EventType;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate strTemplate;
    private final FollowingRepository followingRepository;
    private final NotificationSendService notificationSendService;

    /*
        개설자가 채팅방 종료
     */
    public void mentorEndRoom(Long userId, Long roomId) {
        if (roomId != null) {
            ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElse(null);
            if (userId == chatRoom.getMentor().getId()) {
                LocalDateTime endTime = LocalDateTime.now();

                // 채팅방 종료시간 입력
                chatRoom.setEndedAt(endTime);
                chatRoomRepository.save(chatRoom);

                // 채팅방 참석자들 나가는 시간 입력
                List<ChatParticipant> chatterList = chatParticipantRepository.findAllByRoomAndExitAt(chatRoom, null);
                for (ChatParticipant chatter : chatterList) {
                    chatter.setExitAt(endTime);
                }
                chatParticipantRepository.saveAll(chatterList);

                // 해당 방 종료 알리기( 채팅방에 연결된 웹소켓 통신 종료시키기 )
                String destination = "/chatRoom/" + roomId + "/door";
                messagingTemplate.convertAndSend(destination, "DISCONNECT");

                // 채팅방 목록에서 해당방 삭제
                strTemplate.convertAndSend("room/end", roomId+"");
            }
        }
    }

    /*
        아직 종료안된 단체톡방들 select
     */
    public List<ChatRoom> getChatRoomList() {
        List<ChatRoom> roomList = chatRoomRepository.findAllByEndedAt(null);
        return roomList;
    }

    /*
        채팅방 목록화면, 채팅방List
     */
    public List<ChatRoomListResponse> getChatRoomListView() {

        List<ChatRoom> chatRoomEntityList = chatRoomRepository.findAllByEndedAt(null);
        List<ChatRoomListResponse> chatRoomList = new LinkedList<>();

        for (ChatRoom chatRoomEntity :chatRoomEntityList) {
            chatRoomList.add(
                    chatRoomEntityToDto(chatRoomEntity));
        }
        return chatRoomList;
    }

    /*
        채팅방 목록 화면에서, 채팅방에 필요한 데이터들로 얻기
     */
    public ChatRoomListResponse chatRoomEntityToDto(ChatRoom entity) {
        // count메서드로 바꿔야함
        List<ChatParticipant> currentParticipants = chatParticipantRepository.findAllByRoomAndExitAt(entity, null);
        
        // 날짜 String타입의 원하는 형식으로 변형
        LocalDateTime ldt = entity.getCreatedAt();

        if (ldt == null) {
            ldt = LocalDateTime.now(); // Entity만들때 java쪽에선 null로 들어가고, db쪽에선 CURRENT_TIMESTAMP로 되서
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH시mm분");
        String formattedCreatedAt = ldt.format(formatter);

        ChatRoomListResponse chatRoomDTO = ChatRoomListResponse.builder()
                .id(entity.getId())
                .mentor(entity.getMentor().getNickname())
                .roomName(entity.getRoomName()).createdAt(formattedCreatedAt)
                .userLimit(entity.getUserLimit()).userCount(currentParticipants.size())
                .build();

        return chatRoomDTO;
    }

    /*
        멘토가 단톡방 생성
     */
    @Transactional
    public void mentorCreateRoom(long userId, String roomName, int userLimit) {

        User user = userRepository.findById(userId).orElse(null);

        if (user != null && user.getRole().name() == "MENTOR") {
            ChatRoom room = ChatRoom.builder()
                    .mentor(user).roomName(roomName).userLimit(userLimit)
                    .build();
            ChatRoom createdRoom = chatRoomRepository.save(room);

            ChatRoomListResponse chatRoomListResponse = chatRoomEntityToDto(createdRoom);
            // 채팅방 목록 화면에 생성된 채팅방 추가
            String message = convertToJson(chatRoomListResponse);
            strTemplate.convertAndSend("room/creation", message);

            List<Following> followings = followingRepository.findByToUserId(user.getId());
            log.info("[CHAT] 멘토 {}의 팔로워 수: {}", user.getNickname(), followings.size());

            for (Following following: followings) {
                log.info("[CHAT] 팔로워 {}에게 채팅방 {} 알림 전송 시작",
                        following.getFromUser().getNickname(), createdRoom.getId());

                NotificationSendRequest notificationSendRequest = new NotificationSendRequest(
                        following.getFromUser().getId(),  // 알림 받을 사람 (팔로워)
                        user.getId(),                     // 알림 보낸 사람 (멘토 본인)
                        user.getNickname(),               // 알림 보낸 사람 닉네임
                        EventType.MENTOR_CHAT,            // 이벤트 유형
                        String.valueOf(createdRoom.getId()),  // 채팅방 ID (저장된 객체 사용)
                        false                             // RedisSubscriber에서 온 게 아님
                );

                notificationSendService.sendNotification(notificationSendRequest);
                log.info("[CHAT] 팔로워 {}에게 알림 전송 완료", following.getFromUser().getNickname());
            }
        } else {
            throw new ExpectedException(ErrorCode.FAIL_ROOM_CREATE);
        }
    }

    private String convertToJson(ChatRoomListResponse chatRoomListResponse) {
        try {
            return new ObjectMapper().writeValueAsString(chatRoomListResponse);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingCustomException(e);
        }
    }


}
