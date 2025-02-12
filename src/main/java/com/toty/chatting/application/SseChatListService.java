package com.toty.chatting.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toty.chatting.dto.response.ChatRoomListResponse;
import com.toty.common.baseException.JsonProcessingCustomException;
import com.toty.notification.dto.request.NotificationSendRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseChatListService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
    }

    public void sendEventCountUp(String roomId) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("countUp").data(roomId));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    public void sendEventCountDown(String roomId) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("countDown").data(roomId));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    public void sendEventEndRoom(String roomId) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("roomEnd").data(roomId));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    public void sendEventCreationRoom(String chatRoomJson) {

        ChatRoomListResponse chatRoomListResponse = convertFromJson(chatRoomJson);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("roomCreation").data(chatRoomListResponse));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    private ChatRoomListResponse convertFromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, ChatRoomListResponse.class);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingCustomException(e);
        }
    }
}
