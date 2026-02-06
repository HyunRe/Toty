package com.toty.service;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.notification.application.service.NotificationService;
import com.toty.notification.domain.model.Notification;
import com.toty.notification.domain.repository.NotificationRepository;
import com.toty.notification.domain.type.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationService 단위 테스트
 * 목적: 알림 조회, 읽음 처리, 삭제 로직 검증
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("알림 서비스 단위 테스트")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Long receiverId;
    private Notification unreadNotification1;
    private Notification unreadNotification2;
    private Notification readNotification;

    @BeforeEach
    void setUp() {
        receiverId = 1L;

        unreadNotification1 = new Notification(
                "id1", receiverId, 2L, "발신자1",
                EventType.LIKE, "메시지1", "/url1", false,
                LocalDateTime.now().minusMinutes(10)
        );

        unreadNotification2 = new Notification(
                "id2", receiverId, 3L, "발신자2",
                EventType.COMMENT, "메시지2", "/url2", false,
                LocalDateTime.now().minusMinutes(5)
        );

        readNotification = new Notification(
                "id3", receiverId, 4L, "발신자3",
                EventType.FOLLOW, "메시지3", "/url3", true,
                LocalDateTime.now().minusHours(1)
        );
    }

    @Test
    @DisplayName("읽지 않은 알림 조회 성공 - 최신순 정렬")
    void getUnreadNotificationsSortedByDate_success() {
        // given
        List<Notification> expected = Arrays.asList(unreadNotification2, unreadNotification1);
        when(notificationRepository.findByReceiverIdAndIsReadFalse(
                eq(receiverId),
                any(Sort.class)
        )).thenReturn(expected);

        // when
        List<Notification> result = notificationService.getUnreadNotificationsSortedByDate(receiverId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(unreadNotification2, unreadNotification1);
        verify(notificationRepository).findByReceiverIdAndIsReadFalse(
                eq(receiverId),
                argThat(sort -> sort.getOrderFor("createdAt") != null &&
                        Objects.requireNonNull(sort.getOrderFor("createdAt")).isDescending())
        );
    }

    @Test
    @DisplayName("읽지 않은 알림이 없으면 빈 리스트 반환")
    void getUnreadNotificationsSortedByDate_emptyList() {
        // given
        when(notificationRepository.findByReceiverIdAndIsReadFalse(
                eq(receiverId),
                any(Sort.class)
        )).thenReturn(Collections.emptyList());

        // when
        List<Notification> result = notificationService.getUnreadNotificationsSortedByDate(receiverId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회 성공")
    void countUnreadNotifications_success() {
        // given
        List<Notification> unreadList = Arrays.asList(unreadNotification1, unreadNotification2);
        when(notificationRepository.findByReceiverIdAndIsReadFalse(
                eq(receiverId),
                any(Sort.class)
        )).thenReturn(unreadList);

        // when
        int count = notificationService.countUnreadNotifications(receiverId);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("읽지 않은 알림이 없으면 0 반환")
    void countUnreadNotifications_returnsZero() {
        // given
        when(notificationRepository.findByReceiverIdAndIsReadFalse(
                eq(receiverId),
                any(Sort.class)
        )).thenReturn(Collections.emptyList());

        // when
        int count = notificationService.countUnreadNotifications(receiverId);

        // then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("알림 개수 조회 시 null이면 0 반환")
    void countUnreadNotifications_handlesNull() {
        // given
        when(notificationRepository.findByReceiverIdAndIsReadFalse(
                eq(receiverId),
                any(Sort.class)
        )).thenReturn(null);

        // when
        int count = notificationService.countUnreadNotifications(receiverId);

        // then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 알림 읽음 처리 성공")
    void markAsReadForReceiverAndNotification_success() {
        // given
        String notificationId = "id1";
        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(unreadNotification1));
        doNothing().when(notificationRepository).deleteByReceiverIdAndIsReadTrue(receiverId);

        // when - 비동기 메서드이므로 호출만 검증
        notificationService.markAsReadForReceiverAndNotification(receiverId, notificationId);

        // then
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(argThat(notification ->
                notification.getId().equals(notificationId) &&
                        notification.isRead()
        ));
    }

    @Test
    @DisplayName("이미 읽은 알림을 다시 읽음 처리하면 예외 발생")
    void markAsReadForReceiverAndNotification_alreadyRead_throwsException() {
        // given
        String notificationId = "id3";
        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(readNotification));

        // when & then - 이미 읽은 알림은 필터링되어 empty 반환
        assertThatThrownBy(() ->
                notificationService.markAsReadForReceiverAndNotification(receiverId, notificationId)
        ).isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 알림 읽음 처리 시 예외 발생")
    void markAsReadForReceiverAndNotification_notFound_throwsException() {
        // given
        String notificationId = "nonexistent";
        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                notificationService.markAsReadForReceiverAndNotification(receiverId, notificationId)
        ).isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 사용자의 알림을 읽음 처리하면 예외 발생")
    void markAsReadForReceiverAndNotification_differentReceiver_throwsException() {
        // given
        Long differentReceiverId = 999L;
        String notificationId = "id1";
        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(unreadNotification1));

        // when & then
        assertThatThrownBy(() ->
                notificationService.markAsReadForReceiverAndNotification(differentReceiverId, notificationId)
        ).isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("전체 알림 읽음 처리 성공")
    void markAllAsRead_success() {
        // given
        List<Notification> unreadList = Arrays.asList(unreadNotification1, unreadNotification2);
        when(notificationRepository.findByReceiverIdAndIsReadFalse(
                eq(receiverId),
                any(Sort.class)
        )).thenReturn(unreadList);
        doNothing().when(notificationRepository).deleteByReceiverIdAndIsReadTrue(receiverId);

        // when
        notificationService.markAllAsRead(receiverId);

        // then
        verify(notificationRepository).findByReceiverIdAndIsReadFalse(
                eq(receiverId),
                any(Sort.class)
        );
        verify(notificationRepository).saveAll(argThat(notifications -> {
            List<Notification> list = (List<Notification>) notifications;
            return list.size() == 2 &&
                    list.stream().allMatch(Notification::isRead);
        }));
    }

    @Test
    @DisplayName("읽지 않은 알림이 없을 때 전체 읽음 처리")
    void markAllAsRead_noUnreadNotifications() {
        // given
        when(notificationRepository.findByReceiverIdAndIsReadFalse(
                eq(receiverId),
                any(Sort.class)
        )).thenReturn(Collections.emptyList());

        // when
        notificationService.markAllAsRead(receiverId);

        // then
        verify(notificationRepository).saveAll(Collections.emptyList());
    }

    @Test
    @DisplayName("읽은 알림 삭제 성공")
    void deleteAllReadNotifications_success() {
        // given
        doNothing().when(notificationRepository).deleteByReceiverIdAndIsReadTrue(receiverId);

        // when
        notificationService.deleteAllReadNotifications(receiverId);

        // then
        verify(notificationRepository).deleteByReceiverIdAndIsReadTrue(receiverId);
    }

    @Test
    @DisplayName("여러 사용자의 알림을 독립적으로 조회")
    void getUnreadNotifications_multipleUsers() {
        // given
        Long user1 = 1L;
        Long user2 = 2L;

        Notification user2Notification = new Notification(
                "id4", user2, 5L, "발신자4",
                EventType.COMMENT, "메시지4", "/url4", false,
                LocalDateTime.now()
        );

        when(notificationRepository.findByReceiverIdAndIsReadFalse(eq(user1), any(Sort.class)))
                .thenReturn(List.of(unreadNotification1));
        when(notificationRepository.findByReceiverIdAndIsReadFalse(eq(user2), any(Sort.class)))
                .thenReturn(List.of(user2Notification));

        // when
        List<Notification> result1 = notificationService.getUnreadNotificationsSortedByDate(user1);
        List<Notification> result2 = notificationService.getUnreadNotificationsSortedByDate(user2);

        // then
        assertThat(result1).hasSize(1);
        assertThat(result2).hasSize(1);
        assertThat(result1.get(0).getReceiverId()).isEqualTo(user1);
        assertThat(result2.get(0).getReceiverId()).isEqualTo(user2);
    }
}
