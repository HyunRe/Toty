package com.toty.chat.presentation.view;

import com.toty.chat.application.service.ChatRoomService;
import com.toty.chat.domain.model.ChatParticipant;
import com.toty.chat.domain.model.ChatRoom;
import com.toty.chat.domain.repository.ChatParticipantRepository;
import com.toty.chat.domain.repository.ChatRoomRepository;
import com.toty.chat.dto.response.ChatRoomListResponse;
import com.toty.common.annotation.CurrentUser;
import com.toty.user.domain.model.User;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/view/chatting")
@RequiredArgsConstructor
public class ChatViewController {
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomService chatRoomService;

    @Value("${server.port}") private String serverPort;

    /*
        유저, 단톡방 목록 화면
     */
    @RequestMapping("/list")
    public String chatList(Model model, @CurrentUser User user) {
        List<ChatRoomListResponse> chatRoomList = chatRoomService.getChatRoomListView();
        model.addAttribute("chatRoomList", chatRoomList);
        if (user != null) {
            Long userId = user.getId();
            model.addAttribute("userId", userId);
        }
        return "chatting/chatList";
    }

    /*
                단통방 화면
        단톡방 목록에서
        단톡방별로, 버튼(참여)을 눌러야만 입장가능하도록 설계되있음
        @PostMapping("/participant/{rid}/{uid}")에서 redirect:/view/chatting/room 됨
     */
    @RequestMapping("/room")
    public String aachr(@RequestParam("rid") long rid, @CurrentUser User user, Model model) {

        Optional<ChatRoom> room = chatRoomRepository.findById(rid);

        if (!room.isEmpty()) {
            List<ChatParticipant> chatterList = chatParticipantRepository.findAllByRoomAndExitAt(room.get(), null);
            model.addAttribute("chatterList", chatterList);
            model.addAttribute("room", room.get());
            if (user != null) {
                Long userId = user.getId();
                String nickname = user.getNickname();
                model.addAttribute("userId", userId);
                model.addAttribute("nickname", nickname);
            }
            model.addAttribute("serverPort", serverPort);
        }

        return "chatting/chatRoom";
    }

}
