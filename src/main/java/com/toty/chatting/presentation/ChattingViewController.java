package com.toty.chatting.presentation;


import com.toty.chatting.application.ChatRoomService;
import com.toty.chatting.domain.model.ChatParticipant;
import com.toty.chatting.domain.model.ChatRoom;
import com.toty.chatting.domain.model.Role;
import com.toty.chatting.domain.model.User01;
import com.toty.chatting.domain.repository.ChatParticipantRepository;
import com.toty.chatting.domain.repository.ChatRoomRepository;
import com.toty.chatting.domain.repository.User01Repository;
import com.toty.chatting.dto.response.ChatRoomListResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/view/chatting")
@RequiredArgsConstructor
public class ChattingViewController {

    private final User01Repository user01Repository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomService chatRoomService;

    /*
        유저, 단톡방 목록 화면
     */
    @RequestMapping("/list")
    public String chatList(Model model) {
        List<User01> userList = user01Repository.findAll();
        List<ChatRoom> chatRoomEntityList = chatRoomService.getChatRoomList();

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

        model.addAttribute("userList", userList);
        model.addAttribute("chatRoomList", chatRoomList);
//        model.addAttribute("rowCount", (chatRoomList.size() / 4) + 1);
        return "chatting/chatList";
    }

    /*
                단통방 화면
        단톡방 목록에서
        단톡방별로, 버튼(참여)을 눌러야만 입장가능하도록 설계되있음
        @PostMapping("/participant/{rid}/{uid}")에서 redirect:/view/chatting/room 됨
     */
    @RequestMapping("/room")
    public String aachr(@RequestParam("rid") long rid, Model model) {
        Optional<ChatRoom> room = chatRoomRepository.findById(rid);

        if (!room.isEmpty()) {
            List<ChatParticipant> chatterList = chatParticipantRepository.findAllByRoomAndExitAt(room.get(), null);
            model.addAttribute("chatterList", chatterList);
            model.addAttribute("room", room.get());
        }

        return "chatting/chatRoom";
    }


    /*
        아래 2개는 나중에 삭제할꺼
     */
    // 회원가입(user)
    @PostMapping("/user")
    public String aa12 (@RequestParam("userName") String userName) {
        Role userRole = Role.USER;

        User01 uu = User01.builder()
                .userName(userName).role(userRole)
                .build();

        user01Repository.save(uu);
        return "redirect:/view/chatting/list";
    }

    // 회원가입(mentor)
    @PostMapping("/mentor")
    public String aa (@RequestParam("userName") String userName) {
        Role userRole = Role.MENTOR;

        User01 uu = User01.builder()
                .userName(userName).role(userRole)
                .build();

        user01Repository.save(uu);
        return "redirect:/view/chatting/list";
    }

}
