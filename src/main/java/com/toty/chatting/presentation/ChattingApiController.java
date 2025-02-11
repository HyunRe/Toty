package com.toty.chatting.presentation;

import com.toty.chatting.application.ChatParticipanceService;
import com.toty.chatting.application.ChatRoomService;
import com.toty.chatting.application.User01Service;
import com.toty.chatting.domain.model.ChatRoom;
import com.toty.common.annotation.CurrentUser;
import com.toty.user.domain.model.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/api/chatting")
@RequiredArgsConstructor
public class ChattingApiController {

    private final ChatRoomService chatRoomService;
    private final ChatParticipanceService chatParticipanceService;

    /*
                단톡방 입장하기
        단톡방 목록에서
        단톡방별로, 버튼(참여)을 눌러야만 동작하게 설계되있음
     */
    @PostMapping("/participant/{rid}")
    public String enterRoom( @PathVariable("rid") long rid
            , @CurrentUser User user, RedirectAttributes reAtr) {
        if (user != null) {
            Long userId = user.getId();
            chatParticipanceService.userEnterRoom(rid, userId);
            reAtr.addAttribute("rid", rid);
            return "redirect:/view/chatting/room";
        }
        return "redirect:/view/chatting/list";
    }

    /*
                채팅방 나가기
        단톡방에서 버튼(나가기)을 눌러야만 동작하게 설계되있음
     */
    @PatchMapping("/rooms/{roomId}/{chatterId}")
    @ResponseBody
    public void exitRoom(@PathVariable("roomId") long roomId, @PathVariable("chatterId") long chatterId) {
        chatParticipanceService.chatterExitRoom(roomId, chatterId);
    }

    /*
                채팅방 종료
        단톡방에서 해당방을 개설한 멘토만 동작 가능
     */
    @PatchMapping("/rooms/{roomId}")
    @ResponseBody
    public void endRoom(@PathVariable("roomId") long roomId, @CurrentUser User user) {
        if (user != null) {
            Long userId = user.getId();
            chatRoomService.mentorEndRoom(userId, roomId);
        }
    }

    /*
                단체 채팅방 생성
        validaton필요?
     */
    @PostMapping("/room")
    @ResponseBody
    public void createRoom( @RequestParam("roomName") String roomName, @RequestParam("userLimit") int userLimit
        , @CurrentUser User user) {
        if (user != null) {
            Long userId = user.getId();
            chatRoomService.mentorCreateRoom(userId, roomName, userLimit);
        }
    }

    /*
        단체 채팅방 목록
     */
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> roomList() {
        return chatRoomService.getChatRoomList();
    }
}
