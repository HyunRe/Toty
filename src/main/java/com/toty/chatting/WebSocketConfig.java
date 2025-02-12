package com.toty.chatting;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override // 메시지 브로커 설정
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독할 수 있는 메시지 브로커를 설정. 서버에서 /chatRoom으로 시작하는 경로로 메시지가 전송되면 브로커가 이를 클라이언트에게 전송
        registry.enableSimpleBroker("/chatRoom");
        // 클라이언트가 메시지를 보낼 때 사용할 prefix 설정. 이를 컨트롤러에서 @MessageMapping을 사용해 처리가능
        registry.setApplicationDestinationPrefixes("/chatApp");
    }

    @Override // STOMP 엔드포인트 등록
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트는 해당 경로를 통해 WebSocket서버와 연결, 해당 경로에서 WebSocket연결을 수락
        registry.addEndpoint("/chatRoom-v02-websocket");
    }
}