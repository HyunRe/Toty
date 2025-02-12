package com.toty.chatting.presentation;

import com.toty.chatting.application.SseChatListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
@RequiredArgsConstructor
public class SseChatListController {

    private final SseChatListService sseChatListService;

    @GetMapping(path = "/sse/chatList", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        SseEmitter emitter = new SseEmitter(300_000L); // 300초 타임아웃
        sseChatListService.addEmitter(emitter);
        return emitter;
    }
}
