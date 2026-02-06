package com.toty.service;

import com.toty.notification.application.service.NotificationCreationService;
import com.toty.notification.application.service.NotificationService;
import com.toty.notification.domain.factory.message.NotificationMessageFactory;
import com.toty.notification.domain.factory.url.NotificationUrlFactory;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.domain.repository.NotificationRepository;
import com.toty.notification.domain.type.EventType;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.notification.infrastructure.email.application.service.EmailService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationCreationService 단위 테스트
 * 목적: 알림 생성 로직 및 이메일 전송 조건 검증
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("알림 생성 서비스 단위 테스트")
class NotificationCreationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMessageFactory notificationMessageFactory;

    @Mock
    private NotificationUrlFactory notificationUrlFactory;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationCreationService notificationCreationService;

    private NotificationSendRequest notificationSendRequest;

    @BeforeEach
    void setUp() {
        notificationSendRequest = new NotificationSendRequest(
                1L,          // receiverId
                2L,          // senderId
                "테스터",     // senderNickname
                EventType.LIKE,
                "100",       // referenceId (String)
                false        // fromRedis
        );
    }

    @Test
    @DisplayName("알림 생성 성공 - 메시지와 URL이 생성되고 Redis에 저장된다")
    void createNotification_success() throws MessagingException {
        // given
        String expectedMessage = "테스터님이 좋아요를 눌렀습니다";
        String expectedUrl = "/posts/100";

        when(notificationMessageFactory.generateMessage(EventType.LIKE, "테스터"))
                .thenReturn(expectedMessage);
        when(notificationUrlFactory.generateUrl(EventType.LIKE, "100"))
                .thenReturn(expectedUrl);
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.countUnreadNotifications(1L))
                .thenReturn(5);

        // when
        Notification result = notificationCreationService.createNotification(notificationSendRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReceiverId()).isEqualTo(1L);
        assertThat(result.getSenderId()).isEqualTo(2L);
        assertThat(result.getSenderNickname()).isEqualTo("테스터");
        assertThat(result.getEventType()).isEqualTo(EventType.LIKE);
        assertThat(result.getMessage()).isEqualTo(expectedMessage);
        assertThat(result.getUrl()).isEqualTo(expectedUrl);
        assertThat(result.isRead()).isFalse();
        assertThat(result.getId()).isNotNull();

        // 팩토리 메서드 호출 검증
        verify(notificationMessageFactory).generateMessage(EventType.LIKE, "테스터");
        verify(notificationUrlFactory).generateUrl(EventType.LIKE, "100");

        // Redis 저장 검증
        verify(notificationRepository).save(any(Notification.class));

        // 이메일 전송 조건 확인
        verify(notificationService).countUnreadNotifications(1L);
        verify(emailService, never()).sendEmailNotification(any());
    }

    @Test
    @DisplayName("안읽은 알림 10개 도달 시 이메일 전송")
    void createNotification_sendsEmail_whenUnreadCountIs10() throws MessagingException {
        // given
        when(notificationMessageFactory.generateMessage(any(), anyString()))
                .thenReturn("메시지");
        when(notificationUrlFactory.generateUrl(any(), anyString()))
                .thenReturn("/url");
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.countUnreadNotifications(1L))
                .thenReturn(10);  // 정확히 10개

        // when
        notificationCreationService.createNotification(notificationSendRequest);

        // then - 이메일 전송 호출됨
        verify(emailService).sendEmailNotification(argThat(request ->
                request.getReceiverId().equals(1L) &&
                        request.getMessage().equals("확인하지 않은 알림이 10개 있습니다") &&
                        request.getEventType() == EventType.LIKE
        ));
    }

    @Test
    @DisplayName("안읽은 알림 9개 이하일 때 이메일 전송 안됨")
    void createNotification_noEmail_whenUnreadCountIsLessThan10() throws MessagingException {
        // given
        when(notificationMessageFactory.generateMessage(any(), anyString()))
                .thenReturn("메시지");
        when(notificationUrlFactory.generateUrl(any(), anyString()))
                .thenReturn("/url");
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.countUnreadNotifications(1L))
                .thenReturn(9);  // 9개

        // when
        notificationCreationService.createNotification(notificationSendRequest);

        // then - 이메일 전송 안됨
        verify(emailService, never()).sendEmailNotification(any());
    }

    @Test
    @DisplayName("안읽은 알림 11개 이상일 때 이메일 전송 안됨")
    void createNotification_noEmail_whenUnreadCountIsMoreThan10() throws MessagingException {
        // given
        when(notificationMessageFactory.generateMessage(any(), anyString()))
                .thenReturn("메시지");
        when(notificationUrlFactory.generateUrl(any(), anyString()))
                .thenReturn("/url");
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.countUnreadNotifications(1L))
                .thenReturn(11);  // 11개 (10개 초과)

        // when
        notificationCreationService.createNotification(notificationSendRequest);

        // then - 이메일 전송 안됨
        verify(emailService, never()).sendEmailNotification(any());
    }

    @Test
    @DisplayName("이메일 전송 실패해도 알림 생성은 성공")
    void createNotification_success_evenIfEmailFails() throws MessagingException {
        // given
        when(notificationMessageFactory.generateMessage(any(), anyString()))
                .thenReturn("메시지");
        when(notificationUrlFactory.generateUrl(any(), anyString()))
                .thenReturn("/url");
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.countUnreadNotifications(1L))
                .thenReturn(10);
        doThrow(new RuntimeException("이메일 전송 실패"))
                .when(emailService).sendEmailNotification(any());

        // when & then - 예외 발생하지 않음
        assertThatCode(() -> notificationCreationService.createNotification(notificationSendRequest))
                .doesNotThrowAnyException();

        // 알림은 정상 저장됨
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("다양한 이벤트 타입에 대해 알림 생성")
    void createNotification_variousEventTypes() {
        // given
        EventType[] eventTypes = {
                EventType.LIKE,
                EventType.COMMENT,
                EventType.FOLLOW,
                EventType.MENTOR_POST,
                EventType.QNA_POST
        };

        when(notificationMessageFactory.generateMessage(any(), anyString()))
                .thenReturn("메시지");
        when(notificationUrlFactory.generateUrl(any(), anyString()))
                .thenReturn("/url");
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.countUnreadNotifications(any()))
                .thenReturn(5);

        for (EventType eventType : eventTypes) {
            NotificationSendRequest request = new NotificationSendRequest(
                    1L, 2L, "테스터", eventType, "100", false
            );

            // when
            Notification result = notificationCreationService.createNotification(request);

            // then
            assertThat(result.getEventType()).isEqualTo(eventType);
        }

        // 각 이벤트 타입마다 한 번씩 저장
        verify(notificationRepository, times(eventTypes.length)).save(any(Notification.class));
    }

    @Test
    @DisplayName("알림 ID는 UUID 형식으로 생성된다")
    void createNotification_generatesUuidId() {
        // given
        when(notificationMessageFactory.generateMessage(any(), anyString()))
                .thenReturn("메시지");
        when(notificationUrlFactory.generateUrl(any(), anyString()))
                .thenReturn("/url");
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.countUnreadNotifications(any()))
                .thenReturn(5);

        // when
        Notification result1 = notificationCreationService.createNotification(notificationSendRequest);
        Notification result2 = notificationCreationService.createNotification(notificationSendRequest);

        // then - 서로 다른 UUID 생성
        assertThat(result1.getId()).isNotNull();
        assertThat(result2.getId()).isNotNull();
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
        assertThat(result1.getId()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    @DisplayName("알림 생성 시 isRead는 false로 초기화")
    void createNotification_initiallyUnread() {
        // given
        when(notificationMessageFactory.generateMessage(any(), anyString()))
                .thenReturn("메시지");
        when(notificationUrlFactory.generateUrl(any(), anyString()))
                .thenReturn("/url");
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.countUnreadNotifications(any()))
                .thenReturn(5);

        // when
        Notification result = notificationCreationService.createNotification(notificationSendRequest);

        // then
        assertThat(result.isRead()).isFalse();
    }

    @Test
    @DisplayName("알림 생성 시 createdAt이 현재 시간으로 설정")
    void createNotification_setsCreatedAt() {
        // given
        when(notificationMessageFactory.generateMessage(any(), anyString()))
                .thenReturn("메시지");
        when(notificationUrlFactory.generateUrl(any(), anyString()))
                .thenReturn("/url");
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.countUnreadNotifications(any()))
                .thenReturn(5);

        // when
        Notification result = notificationCreationService.createNotification(notificationSendRequest);

        // then
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getCreatedAt()).isBeforeOrEqualTo(java.time.LocalDateTime.now());
    }
}
