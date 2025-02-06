package com.toty.chatting.application;

import com.toty.chatting.domain.model.ChatParticipant;
import com.toty.chatting.domain.model.ChatRoom;
import com.toty.chatting.domain.model.User01;
import com.toty.chatting.domain.repository.ChatParticipantRepository;
import com.toty.chatting.domain.repository.ChatRoomRepository;
import com.toty.chatting.domain.repository.User01Repository;
import com.toty.chatting.dto.response.ChatRoomListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final User01Repository user01Repository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final SimpMessagingTemplate messagingTemplate;


    /*
        개설자가 채팅방 종료
     */
    public void mentorEndRoom(long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElse(null);
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
            // count메서드로 바꿔야함
            List<ChatParticipant> currentParticipants = chatParticipantRepository.findAllByRoomAndExitAt(chatRoomEntity, null);

            ChatRoomListResponse chatRoom = ChatRoomListResponse.builder()
                    .id(chatRoomEntity.getId())
                    .mentor(chatRoomEntity.getMentor().getUserName())
                    .roomName(chatRoomEntity.getRoomName()).createdAt(chatRoomEntity.getCreatedAt())
                    .userLimit(chatRoomEntity.getUserLimit()).userCount(currentParticipants.size())
                    .build();

            chatRoomList.add(chatRoom);
        }
        return chatRoomList;
    }


    /*
        멘토가 단톡방 생성
     */
    public void mentorCreateRoom(long mid, String roomName, int userLimit) {

        User01 mentor = user01Repository.findById(mid).orElse(null);

        if (mentor != null && mentor.getRole().name() == "MENTOR") {
            ChatRoom room = ChatRoom.builder()
                    .mentor(mentor).roomName(roomName).userLimit(userLimit)
                    .build();
            chatRoomRepository.save(room);
        } else {
            // throw new Exception();
        }
    }


}
