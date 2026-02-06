package com.toty.infrastructure;

import com.toty.common.sse.application.sender.SseNotificationSender;
import com.toty.common.sse.domain.SseEmitterType;
import com.toty.common.sse.dto.SseNotificationSendRequest;
import com.toty.notification.application.sse.NotificationSseService;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.domain.type.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SseNotificationSender 단위 테스트
 * 목적: SSE를 통한 알림 전송 로직 검증
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("SSE 알림 전송 인프라 테스트")
class SseNotificationSenderTest {

    @Mock
    private NotificationSseService notificationSseService;

    @InjectMocks
    private SseNotificationSender sseNotificationSender;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification(
                "notification-id",
                1L,
                2L,
                "발신자",
                EventType.LIKE,
                "발신자님이 좋아요를 눌렀습니다",
                "/posts/100",
                false,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("SINGLE 모드로 SSE 알림 전송 성공")
    void send_singleMode_success() {
        // given
        when(notificationSseService.getEmitterType()).thenReturn(SseEmitterType.SINGLE);
        doNothing().when(notificationSseService).sendNotification(any(SseNotificationSendRequest.class));

        // when
        sseNotificationSender.send(notification);

        // then
        verify(notificationSseService).getEmitterType();
        verify(notificationSseService).sendNotification(argThat(request ->
                request.getId().equals("notification-id") &&
                        request.getReceiverId().equals(1L) &&
                        request.getSenderId().equals(2L) &&
                        request.getSenderNickname().equals("발신자") &&
                        request.getEventType() == EventType.LIKE &&
                        request.getMessage().equals("발신자님이 좋아요를 눌렀습니다") &&
                        request.getUrl().equals("/posts/100")
        ));
        verify(notificationSseService, never()).sendMultipleNotifications(anyList());
    }

    @Test
    @DisplayName("MULTI 모드로 SSE 알림 전송 성공")
    void send_multiMode_success() {
        // given
        when(notificationSseService.getEmitterType()).thenReturn(SseEmitterType.MULTI);
        doNothing().when(notificationSseService).sendMultipleNotifications(anyList());

        // when
        sseNotificationSender.send(notification);

        // then
        verify(notificationSseService).getEmitterType();
        verify(notificationSseService).sendMultipleNotifications(argThat(list -> {
            List<SseNotificationSendRequest> requestList = (List<SseNotificationSendRequest>) list;
            return requestList.size() == 1 &&
                    requestList.get(0).getId().equals("notification-id");
        }));
        verify(notificationSseService, never()).sendNotification(any());
    }

    @Test
    @DisplayName("EmitterType이 null이면 기본값으로 SINGLE 전송")
    void send_nullEmitterType_fallbackToSingle() {
        // given
        when(notificationSseService.getEmitterType()).thenReturn(null);
        doNothing().when(notificationSseService).sendNotification(any(SseNotificationSendRequest.class));

        // when
        sseNotificationSender.send(notification);

        // then
        verify(notificationSseService).sendNotification(any(SseNotificationSendRequest.class));
        verify(notificationSseService, never()).sendMultipleNotifications(anyList());
    }

    @Test
    @DisplayName("다양한 이벤트 타입에 대해 SSE 전송")
    void send_variousEventTypes() {
        // given
        EventType[] eventTypes = {
                EventType.LIKE,
                EventType.COMMENT,
                EventType.FOLLOW,
                EventType.MENTOR_POST,
                EventType.QNA_POST
        };

        when(notificationSseService.getEmitterType()).thenReturn(SseEmitterType.SINGLE);
        doNothing().when(notificationSseService).sendNotification(any());

        for (EventType eventType : eventTypes) {
            Notification testNotification = new Notification(
                    "id-" + eventType,
                    1L,
                    2L,
                    "발신자",
                    eventType,
                    "메시지",
                    "/url",
                    false,
                    LocalDateTime.now()
            );

            // when
            sseNotificationSender.send(testNotification);
        }

        // then - 각 이벤트 타입마다 한 번씩 전송
        verify(notificationSseService, times(eventTypes.length)).sendNotification(any());
    }

    @Test
    @DisplayName("SSE 전송 시 notification의 모든 정보가 포함된다")
    void send_includesAllNotificationInfo() {
        // given
        when(notificationSseService.getEmitterType()).thenReturn(SseEmitterType.SINGLE);
        doNothing().when(notificationSseService).sendNotification(any());

        // when
        sseNotificationSender.send(notification);

        // then
        verify(notificationSseService).sendNotification(argThat(request ->
                request.getId().equals(notification.getId()) &&
                        request.getReceiverId().equals(notification.getReceiverId()) &&
                        request.getSenderId().equals(notification.getSenderId()) &&
                        request.getSenderNickname().equals(notification.getSenderNickname()) &&
                        request.getEventType() == notification.getEventType() &&
                        request.getMessage().equals(notification.getMessage()) &&
                        request.getUrl().equals(notification.getUrl())
        ));
    }

    @Test
    @DisplayName("referenceId는 빈 문자열로 설정된다")
    void send_referenceIdIsEmpty() {
        // given
        when(notificationSseService.getEmitterType()).thenReturn(SseEmitterType.SINGLE);
        doNothing().when(notificationSseService).sendNotification(any());

        // when
        sseNotificationSender.send(notification);

        // then
        verify(notificationSseService).sendNotification(argThat(request ->
                request.getReferenceId().isEmpty()
        ));
    }

    @Test
    @DisplayName("여러 알림을 순차적으로 전송")
    void send_multipleNotifications_sequentially() {
        // given
        Notification notification1 = new Notification("id1", 1L, 2L, "발신자1", EventType.LIKE, "메시지1", "/url1", false, LocalDateTime.now());
        Notification notification2 = new Notification("id2", 1L, 3L, "발신자2", EventType.COMMENT, "메시지2", "/url2", false, LocalDateTime.now());
        Notification notification3 = new Notification("id3", 1L, 4L, "발신자3", EventType.FOLLOW, "메시지3", "/url3", false, LocalDateTime.now());

        when(notificationSseService.getEmitterType()).thenReturn(SseEmitterType.SINGLE);
        doNothing().when(notificationSseService).sendNotification(any());

        // when
        sseNotificationSender.send(notification1);
        sseNotificationSender.send(notification2);
        sseNotificationSender.send(notification3);

        // then
        verify(notificationSseService, times(3)).sendNotification(any());
    }
}
