package com.toty.chat.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toty.chat.dto.response.ChatRoomListResponse;
import com.toty.common.baseException.JsonProcessingCustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 채팅방 메시징 서비스
 * - WebSocket 메시지 전송
 * - Redis Pub/Sub 메시지 전송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomMessagingService {
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate strTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 채팅방 종료 메시지 전송 (WebSocket)
     */
    public void sendRoomClosedMessage(Long roomId) {
        String destination = "/chatRoom/" + roomId + "/door";
        messagingTemplate.convertAndSend(destination, "DISCONNECT");
        log.info("채팅방 종료 메시지 전송 완료 - roomId: {}", roomId);
    }

    /**
     * 채팅방 목록에서 삭제 메시지 전송 (Redis Pub/Sub)
     */
    public void publishRoomEndedEvent(Long roomId) {
        strTemplate.convertAndSend("room/end", String.valueOf(roomId));
        log.info("채팅방 종료 이벤트 발행 완료 - roomId: {}", roomId);
    }

    /**
     * 채팅방 생성 메시지 전송 (Redis Pub/Sub)
     */
    public void publishRoomCreatedEvent(ChatRoomListResponse chatRoom) {
        String message = convertToJson(chatRoom);
        strTemplate.convertAndSend("room/creation", message);
        log.info("채팅방 생성 이벤트 발행 완료 - roomId: {}, roomName: {}",
                chatRoom.getId(), chatRoom.getRoomName());
    }

    /**
     * 객체를 JSON 문자열로 변환
     */
    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패: {}", e.getMessage(), e);
            throw new JsonProcessingCustomException(e);
        }
    }
}
