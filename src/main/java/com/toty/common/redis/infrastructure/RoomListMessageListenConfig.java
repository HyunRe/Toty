package com.toty.common.redis.infrastructure;

import com.toty.chat.application.sse.ChatListSseService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RoomListMessageListenConfig {
    @Bean
    RedisMessageListenerContainer chatRoomListContainer(RedisConnectionFactory connectionFactory
            ,@Qualifier("countUpListener") MessageListenerAdapter countUpListener
            ,@Qualifier("countDownListener") MessageListenerAdapter countDownListener
            ,@Qualifier("creationRoomListener") MessageListenerAdapter creationRoomListener
            ,@Qualifier("endRoomListener") MessageListenerAdapter endRoomListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(countUpListener, new PatternTopic("participant/up"));
        container.addMessageListener(countDownListener, new PatternTopic("participant/down"));
        container.addMessageListener(creationRoomListener, new PatternTopic("room/creation"));
        container.addMessageListener(endRoomListener, new PatternTopic("room/end"));
        return container;
    }

    @Bean(name = "countUpListener")
    MessageListenerAdapter countUpListener(ChatListSseService chatListSseService) {
        return new MessageListenerAdapter(chatListSseService, "sendEventCountUp");
    }

    @Bean(name = "countDownListener")
    MessageListenerAdapter countDownListener(ChatListSseService chatListSseService) {
        return new MessageListenerAdapter(chatListSseService, "sendEventCountDown");
    }

    @Bean(name = "endRoomListener")
    MessageListenerAdapter endRoomListener(ChatListSseService chatListSseService) {
        return new MessageListenerAdapter(chatListSseService, "sendEventEndRoom");
    }

    @Bean(name = "creationRoomListener")
    MessageListenerAdapter creationRoomListener(ChatListSseService chatListSseService) {
        return new MessageListenerAdapter(chatListSseService, "sendEventCreationRoom");
    }

    @Bean
    StringRedisTemplate strTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

}
