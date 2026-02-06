package com.toty.infrastructure;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.domain.type.EventType;
import com.toty.notification.infrastructure.firebase.application.sender.FcmNotificationSender;
import com.toty.notification.infrastructure.firebase.application.service.FcmTokenService;
import com.toty.notification.infrastructure.firebase.application.service.FirebaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FcmNotificationSender 단위 테스트
 * 목적: FCM 푸시 알림 전송 로직 검증
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("FCM 푸시 알림 전송 인프라 테스트")
class FcmNotificationSenderTest {

    @Mock
    private FcmTokenService fcmTokenService;

    @Mock
    private FirebaseService firebaseService;

    @InjectMocks
    private FcmNotificationSender fcmNotificationSender;

    private Notification notification;
    private Long receiverId;

    @BeforeEach
    void setUp() {
        receiverId = 1L;
        notification = new Notification(
                "notification-id",
                receiverId,
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
    @DisplayName("FCM 알림 전송 성공 - 단일 토큰")
    void send_success_singleToken() throws FirebaseMessagingException {
        // given
        List<String> tokens = List.of("token123");
        when(fcmTokenService.getActiveTokensByUserId(receiverId)).thenReturn(tokens);
        doNothing().when(firebaseService).sendFcmNotification(
                anyList(), anyString(), anyString(), anyString(), anyString(), anyString(), any()
        );

        // when
        fcmNotificationSender.send(notification);

        // then
        verify(fcmTokenService).getActiveTokensByUserId(receiverId);
        verify(firebaseService).sendFcmNotification(
                eq(tokens),
                eq("TOTY - 좋아요"),
                eq("발신자님이 좋아요를 눌렀습니다"),
                eq("LIKE"),
                eq("notification-id"),
                eq("/posts/100"),
                isNull()
        );
    }

    @Test
    @DisplayName("FCM 알림 전송 성공 - 여러 토큰")
    void send_success_multipleTokens() throws FirebaseMessagingException {
        // given
        List<String> tokens = Arrays.asList("token1", "token2", "token3");
        when(fcmTokenService.getActiveTokensByUserId(receiverId)).thenReturn(tokens);
        doNothing().when(firebaseService).sendFcmNotification(
                anyList(), anyString(), anyString(), anyString(), anyString(), anyString(), any()
        );

        // when
        fcmNotificationSender.send(notification);

        // then
        verify(firebaseService).sendFcmNotification(
                eq(tokens),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                isNull()
        );
    }

    @Test
    @DisplayName("FCM 알림 전송 실패 - 활성 토큰 없음")
    void send_noActiveTokens_doesNotSend() throws FirebaseMessagingException {
        // given
        when(fcmTokenService.getActiveTokensByUserId(receiverId)).thenReturn(Collections.emptyList());

        // when
        fcmNotificationSender.send(notification);

        // then
        verify(fcmTokenService).getActiveTokensByUserId(receiverId);
        verify(firebaseService, never()).sendFcmNotification(
                anyList(), anyString(), anyString(), anyString(), anyString(), anyString(), any()
        );
    }

    @Test
    @DisplayName("다양한 이벤트 타입에 맞는 제목 생성")
    void send_generatesCorrectTitleForEventTypes() throws FirebaseMessagingException {
        // given
        List<String> tokens = List.of("token123");
        when(fcmTokenService.getActiveTokensByUserId(anyLong())).thenReturn(tokens);
        doNothing().when(firebaseService).sendFcmNotification(
                anyList(), anyString(), anyString(), anyString(), anyString(), anyString(), any()
        );

        // COMMENT 이벤트
        Notification commentNotification = new Notification(
                "id1", receiverId, 2L, "발신자", EventType.COMMENT,
                "메시지", "/url", false, LocalDateTime.now()
        );

        // FOLLOW 이벤트
        Notification followNotification = new Notification(
                "id2", receiverId, 2L, "발신자", EventType.FOLLOW,
                "메시지", "/url", false, LocalDateTime.now()
        );

        // MENTOR_POST 이벤트
        Notification mentorPostNotification = new Notification(
                "id3", receiverId, 2L, "발신자", EventType.MENTOR_POST,
                "메시지", "/url", false, LocalDateTime.now()
        );

        // when & then
        fcmNotificationSender.send(commentNotification);
        verify(firebaseService).sendFcmNotification(
                anyList(), eq("TOTY - 새 댓글"), anyString(), anyString(), anyString(), anyString(), any()
        );

        fcmNotificationSender.send(followNotification);
        verify(firebaseService).sendFcmNotification(
                anyList(), eq("TOTY - 새 팔로워"), anyString(), anyString(), anyString(), anyString(), any()
        );

        fcmNotificationSender.send(mentorPostNotification);
        verify(firebaseService).sendFcmNotification(
                anyList(), eq("TOTY - 멘토 게시글"), anyString(), anyString(), anyString(), anyString(), any()
        );
    }

    @Test
    @DisplayName("BECOME_MENTOR 이벤트 제목 생성")
    void send_becomeMentorEventType() throws FirebaseMessagingException {
        // given
        List<String> tokens = List.of("token123");
        when(fcmTokenService.getActiveTokensByUserId(receiverId)).thenReturn(tokens);
        doNothing().when(firebaseService).sendFcmNotification(
                anyList(), anyString(), anyString(), anyString(), anyString(), anyString(), any()
        );

        Notification becomeMentorNotification = new Notification(
                "id", receiverId, 2L, "발신자", EventType.BECOME_MENTOR,
                "멘토로 선정되었습니다", "/url", false, LocalDateTime.now()
        );

        // when
        fcmNotificationSender.send(becomeMentorNotification);

        // then
        verify(firebaseService).sendFcmNotification(
                anyList(), eq("TOTY - 멘토 선정"), anyString(), anyString(), anyString(), anyString(), any()
        );
    }

    @Test
    @DisplayName("REVOKE_MENTOR 이벤트 제목 생성")
    void send_revokeMentorEventType() throws FirebaseMessagingException {
        // given
        List<String> tokens = List.of("token123");
        when(fcmTokenService.getActiveTokensByUserId(receiverId)).thenReturn(tokens);
        doNothing().when(firebaseService).sendFcmNotification(
                anyList(), anyString(), anyString(), anyString(), anyString(), anyString(), any()
        );

        Notification revokeMentorNotification = new Notification(
                "id", receiverId, 2L, "발신자", EventType.REVOKE_MENTOR,
                "멘토 자격이 해제되었습니다", "/url", false, LocalDateTime.now()
        );

        // when
        fcmNotificationSender.send(revokeMentorNotification);

        // then
        verify(firebaseService).sendFcmNotification(
                anyList(), eq("TOTY - 멘토 해제"), anyString(), anyString(), anyString(), anyString(), any()
        );
    }

    @Test
    @DisplayName("FCM 전송 시 FirebaseMessagingException 발생")
    void send_throwsFirebaseMessagingException() throws FirebaseMessagingException {
        // given
        List<String> tokens = List.of("token123");
        when(fcmTokenService.getActiveTokensByUserId(receiverId)).thenReturn(tokens);
        doThrow(new RuntimeException("FCM 전송 실패"))
                .when(firebaseService).sendFcmNotification(
                        anyList(), anyString(), anyString(), anyString(), anyString(), anyString(), any()
                );

        // when & then
        assertThatThrownBy(() -> fcmNotificationSender.send(notification))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("FCM 전송 실패");
    }

    @Test
    @DisplayName("알림 정보가 FCM 페이로드에 모두 포함된다")
    void send_includesAllNotificationInfo() throws FirebaseMessagingException {
        // given
        List<String> tokens = List.of("token123");
        when(fcmTokenService.getActiveTokensByUserId(receiverId)).thenReturn(tokens);
        doNothing().when(firebaseService).sendFcmNotification(
                anyList(), anyString(), anyString(), anyString(), anyString(), anyString(), any()
        );

        // when
        fcmNotificationSender.send(notification);

        // then
        verify(firebaseService).sendFcmNotification(
                eq(tokens),                                      // tokens
                eq("TOTY - 좋아요"),                             // title
                eq("발신자님이 좋아요를 눌렀습니다"),              // body
                eq("LIKE"),                                      // type
                eq("notification-id"),                           // notificationId
                eq("/posts/100"),                                // url
                isNull()                                         // extraData
        );
    }

    @Test
    @DisplayName("여러 사용자에게 독립적으로 FCM 전송")
    void send_toMultipleUsers() throws FirebaseMessagingException {
        // given
        Long user1 = 1L;
        Long user2 = 2L;

        List<String> user1Tokens = List.of("token1");
        List<String> user2Tokens = List.of("token2", "token3");

        when(fcmTokenService.getActiveTokensByUserId(user1)).thenReturn(user1Tokens);
        when(fcmTokenService.getActiveTokensByUserId(user2)).thenReturn(user2Tokens);
        doNothing().when(firebaseService).sendFcmNotification(
                anyList(), anyString(), anyString(), anyString(), anyString(), anyString(), any()
        );

        Notification notification1 = new Notification(
                "id1", user1, 3L, "발신자1", EventType.LIKE, "메시지1", "/url1", false, LocalDateTime.now()
        );
        Notification notification2 = new Notification(
                "id2", user2, 3L, "발신자2", EventType.COMMENT, "메시지2", "/url2", false, LocalDateTime.now()
        );

        // when
        fcmNotificationSender.send(notification1);
        fcmNotificationSender.send(notification2);

        // then
        verify(firebaseService).sendFcmNotification(
                eq(user1Tokens), anyString(), anyString(), anyString(), anyString(), anyString(), any()
        );
        verify(firebaseService).sendFcmNotification(
                eq(user2Tokens), anyString(), anyString(), anyString(), anyString(), anyString(), any()
        );
    }
}
