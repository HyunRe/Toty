package com.toty.chatting.presentation;

import com.toty.chatting.application.ChatParticipanceService;
import com.toty.chatting.application.ChatRoomService;
import com.toty.chatting.application.User01Service;
import com.toty.chatting.domain.model.ChatRoom;
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
    private final User01Service user01Service;

    /*
                단톡방 입장하기
        단톡방 목록에서
        단톡방별로, 버튼(참여)을 눌러야만 동작하게 설계되있음
     */
    @PostMapping("/participant/{rid}/{uid}")
    public String enterRoom(@PathVariable("rid") long rid, @PathVariable("uid") long uid
            , RedirectAttributes reAtr) {
        chatParticipanceService.userEnterRoom(rid, uid);

        reAtr.addAttribute("rid", rid);
        return "redirect:/view/chatting/room";
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
    public void endRoom(@PathVariable("roomId") long roomId) {
        chatRoomService.mentorEndRoom(roomId);
    }

    /*
                단체 채팅방 생성
        validaton필요?
     */
    @PostMapping("/room/{mid}")
    @ResponseBody
    public void createRoom( @PathVariable("mid") long mid
            , @RequestParam("roomName") String roomName, @RequestParam("userLimit") int userLimit) {
        chatRoomService.mentorCreateRoom(mid, roomName, userLimit);
    }

    /*
        단체 채팅방 목록
     */
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> roomList() {
        return chatRoomService.getChatRoomList();
    }


    // 나중에 삭제할꺼, 사용자 로그인
    @GetMapping("/login/{id}")
    @ResponseBody
    public String userLogin(HttpSession session, @PathVariable("id") long id) {
        String result = user01Service.findUserAndLogin(session, id);
        return result;
    }

}
