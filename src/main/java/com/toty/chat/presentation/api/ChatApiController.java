package com.toty.chat.presentation.api;

import com.toty.chat.application.service.ChatParticipanceService;
import com.toty.chat.application.service.ChatRoomService;
import com.toty.chat.domain.model.ChatRoom;
import com.toty.common.annotation.CurrentUser;
import com.toty.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Tag(name = "Chat", description = "채팅 API")
@Slf4j
@Controller
@RequestMapping("/api/chatting")
@RequiredArgsConstructor
public class ChatApiController {
    private final ChatRoomService chatRoomService;
    private final ChatParticipanceService chatParticipanceService;

    /*
                단톡방 입장하기
        단톡방 목록에서
        단톡방별로, 버튼(참여)을 눌러야만 동작하게 설계되있음
     */
    @Operation(summary = "채팅방 입장", description = "단체 채팅방에 참여합니다")
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
    @Operation(summary = "채팅방 나가기", description = "현재 참여 중인 채팅방에서 나갑니다")
    @PatchMapping("/rooms/{roomId}/{chatterId}")
    @ResponseBody
    public void exitRoom(@PathVariable("roomId") long roomId, @PathVariable("chatterId") long chatterId) {
        chatParticipanceService.chatterExitRoom(roomId, chatterId);
    }

    /*
                채팅방 종료
        단톡방에서 해당방을 개설한 멘토만 동작 가능
     */
    @Operation(summary = "채팅방 종료", description = "[멘토 전용] 자신이 개설한 채팅방을 종료합니다")
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
    @Operation(summary = "채팅방 생성", description = "[멘토 전용] 새로운 단체 채팅방을 생성합니다")
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
    @Operation(summary = "채팅방 목록 조회", description = "전체 단체 채팅방 목록을 조회합니다")
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> roomList() {
        return chatRoomService.getChatRoomList();
    }
}
