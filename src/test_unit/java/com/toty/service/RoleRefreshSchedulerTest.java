package com.toty.service;

import com.toty.common.baseException.NotificationSendException;
import com.toty.following.application.FollowingService;
import com.toty.notification.application.service.NotificationSendService;
import com.toty.notification.domain.type.EventType;
import com.toty.roleRefreshScheduler.application.RoleRefreshScheduler;
import com.toty.roleRefreshScheduler.dto.UserIdAndRoleDto;
import com.toty.user.application.UserService;
import com.toty.user.domain.model.Role;
import com.toty.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RoleRefreshScheduler 단위 테스트
 * 목적: 멘토 승급 스케줄러 로직 검증 (팔로워 100명 기준)
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("멘토 역할 갱신 스케줄러 서비스 테스트")
class RoleRefreshSchedulerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private FollowingService followingService;

    @Mock
    private NotificationSendService notificationSendService;

    @InjectMocks
    private RoleRefreshScheduler roleRefreshScheduler;

    @BeforeEach
    void setUp() {
        // Mock 초기화
    }

    @Test
    @DisplayName("팔로워 100명 이상인 USER를 MENTOR로 승급")
    void refreshRole_promotesToMentor() {
        // given
        Long userId = 1L;
        UserIdAndRoleDto user = new UserIdAndRoleDto(userId, Role.USER);

        when(userRepository.findAllByIsDeletedFalse()).thenReturn(List.of(user));
        when(followingService.countFollowers(userId)).thenReturn(100L); // 정확히 100명
        doNothing().when(userService).updateUserRole(any());
        doNothing().when(notificationSendService).sendNotification(any());

        // when
        roleRefreshScheduler.refreshRole();

        // then
        verify(userService).updateUserRole(argThat(dto ->
                dto.getId().equals(userId) && dto.getRole() == Role.MENTOR
        ));
        verify(notificationSendService).sendNotification(argThat(request ->
                request.getReceiverId().equals(userId) &&
                        request.getSenderId().equals(RoleRefreshScheduler.SYSTEM_SENDER_ID) &&
                        request.getSenderNickname().equals("시스템") &&
                        request.getEventType() == EventType.BECOME_MENTOR
        ));
    }

    @Test
    @DisplayName("팔로워 100명 미만인 MENTOR를 USER로 강등")
    void refreshRole_demotesToUser() {
        // given
        Long userId = 2L;
        UserIdAndRoleDto user = new UserIdAndRoleDto(userId, Role.MENTOR);

        when(userRepository.findAllByIsDeletedFalse()).thenReturn(List.of(user));
        when(followingService.countFollowers(userId)).thenReturn(99L); // 100명 미만
        doNothing().when(userService).updateUserRole(any());
        doNothing().when(notificationSendService).sendNotification(any());

        // when
        roleRefreshScheduler.refreshRole();

        // then
        verify(userService).updateUserRole(argThat(dto ->
                dto.getId().equals(userId) && dto.getRole() == Role.USER
        ));
        verify(notificationSendService).sendNotification(argThat(request ->
                request.getReceiverId().equals(userId) &&
                        request.getEventType() == EventType.REVOKE_MENTOR
        ));
    }

    @Test
    @DisplayName("이미 MENTOR이고 팔로워 100명 이상이면 역할 변경 없음")
    void refreshRole_noChange_alreadyMentor() {
        // given
        Long userId = 3L;
        UserIdAndRoleDto user = new UserIdAndRoleDto(userId, Role.MENTOR);

        when(userRepository.findAllByIsDeletedFalse()).thenReturn(List.of(user));
        when(followingService.countFollowers(userId)).thenReturn(150L); // 100명 이상

        // when
        roleRefreshScheduler.refreshRole();

        // then
        verify(userService, never()).updateUserRole(any());
        verify(notificationSendService, never()).sendNotification(any());
    }

    @Test
    @DisplayName("이미 USER이고 팔로워 100명 미만이면 역할 변경 없음")
    void refreshRole_noChange_alreadyUser() {
        // given
        Long userId = 4L;
        UserIdAndRoleDto user = new UserIdAndRoleDto(userId, Role.USER);

        when(userRepository.findAllByIsDeletedFalse()).thenReturn(List.of(user));
        when(followingService.countFollowers(userId)).thenReturn(50L); // 100명 미만

        // when
        roleRefreshScheduler.refreshRole();

        // then
        verify(userService, never()).updateUserRole(any());
        verify(notificationSendService, never()).sendNotification(any());
    }

    @Test
    @DisplayName("여러 사용자의 역할을 한 번에 갱신")
    void refreshRole_multipleUsers() {
        // given
        UserIdAndRoleDto user1 = new UserIdAndRoleDto(1L, Role.USER);      // 100명 -> MENTOR 승급
        UserIdAndRoleDto user2 = new UserIdAndRoleDto(2L, Role.MENTOR);    // 150명 -> 변경 없음
        UserIdAndRoleDto user3 = new UserIdAndRoleDto(3L, Role.MENTOR);    // 50명 -> USER 강등
        UserIdAndRoleDto user4 = new UserIdAndRoleDto(4L, Role.USER);      // 20명 -> 변경 없음

        when(userRepository.findAllByIsDeletedFalse())
                .thenReturn(Arrays.asList(user1, user2, user3, user4));
        when(followingService.countFollowers(1L)).thenReturn(100L);
        when(followingService.countFollowers(2L)).thenReturn(150L);
        when(followingService.countFollowers(3L)).thenReturn(50L);
        when(followingService.countFollowers(4L)).thenReturn(20L);
        doNothing().when(userService).updateUserRole(any());
        doNothing().when(notificationSendService).sendNotification(any());

        // when
        roleRefreshScheduler.refreshRole();

        // then
        // user1: USER -> MENTOR
        verify(userService).updateUserRole(argThat(dto ->
                dto.getId().equals(1L) && dto.getRole() == Role.MENTOR
        ));
        verify(notificationSendService).sendNotification(argThat(request ->
                request.getReceiverId().equals(1L) && request.getEventType() == EventType.BECOME_MENTOR
        ));

        // user3: MENTOR -> USER
        verify(userService).updateUserRole(argThat(dto ->
                dto.getId().equals(3L) && dto.getRole() == Role.USER
        ));
        verify(notificationSendService).sendNotification(argThat(request ->
                request.getReceiverId().equals(3L) && request.getEventType() == EventType.REVOKE_MENTOR
        ));

        // 총 2번의 역할 변경
        verify(userService, times(2)).updateUserRole(any());
        verify(notificationSendService, times(2)).sendNotification(any());
    }

    @Test
    @DisplayName("경계값 테스트 - 팔로워 정확히 100명")
    void refreshRole_exactlyMinimumFollowers() {
        // given
        Long userId = 5L;
        UserIdAndRoleDto user = new UserIdAndRoleDto(userId, Role.USER);

        when(userRepository.findAllByIsDeletedFalse()).thenReturn(List.of(user));
        when(followingService.countFollowers(userId)).thenReturn(100L); // 정확히 100명
        doNothing().when(userService).updateUserRole(any());
        doNothing().when(notificationSendService).sendNotification(any());

        // when
        roleRefreshScheduler.refreshRole();

        // then - 100명일 때 MENTOR로 승급
        verify(userService).updateUserRole(argThat(dto ->
                dto.getRole() == Role.MENTOR
        ));
    }

    @Test
    @DisplayName("경계값 테스트 - 팔로워 99명")
    void refreshRole_oneLessThanMinimumFollowers() {
        // given
        Long userId = 6L;
        UserIdAndRoleDto user = new UserIdAndRoleDto(userId, Role.MENTOR);

        when(userRepository.findAllByIsDeletedFalse()).thenReturn(List.of(user));
        when(followingService.countFollowers(userId)).thenReturn(99L); // 99명
        doNothing().when(userService).updateUserRole(any());
        doNothing().when(notificationSendService).sendNotification(any());

        // when
        roleRefreshScheduler.refreshRole();

        // then - 99명일 때 USER로 강등
        verify(userService).updateUserRole(argThat(dto ->
                dto.getRole() == Role.USER
        ));
    }

    @Test
    @DisplayName("사용자 목록이 비어있을 때 아무 작업도 하지 않음")
    void refreshRole_emptyUserList() {
        // given
        when(userRepository.findAllByIsDeletedFalse()).thenReturn(Collections.emptyList());

        // when
        roleRefreshScheduler.refreshRole();

        // then
        verify(followingService, never()).countFollowers(anyLong());
        verify(userService, never()).updateUserRole(any());
        verify(notificationSendService, never()).sendNotification(any());
    }

    @Test
    @DisplayName("알림 전송 실패 시 NotificationSendException 발생")
    void refreshRole_notificationFails_throwsException() {
        // given
        Long userId = 7L;
        UserIdAndRoleDto user = new UserIdAndRoleDto(userId, Role.USER);

        when(userRepository.findAllByIsDeletedFalse()).thenReturn(List.of(user));
        when(followingService.countFollowers(userId)).thenReturn(100L);
        doNothing().when(userService).updateUserRole(any());
        doThrow(new RuntimeException("알림 전송 실패"))
                .when(notificationSendService).sendNotification(any());

        // when & then
        assertThatThrownBy(() -> roleRefreshScheduler.refreshRole())
                .isInstanceOf(NotificationSendException.class);

        // 역할은 변경됨
        verify(userService).updateUserRole(any());
    }

    @Test
    @DisplayName("팔로워 0명인 사용자는 USER 유지")
    void refreshRole_zeroFollowers_remainsUser() {
        // given
        Long userId = 8L;
        UserIdAndRoleDto user = new UserIdAndRoleDto(userId, Role.USER);

        when(userRepository.findAllByIsDeletedFalse()).thenReturn(List.of(user));
        when(followingService.countFollowers(userId)).thenReturn(0L);

        // when
        roleRefreshScheduler.refreshRole();

        // then
        verify(userService, never()).updateUserRole(any());
    }

    @Test
    @DisplayName("팔로워 1000명인 사용자는 MENTOR 유지")
    void refreshRole_manyFollowers_remainsMentor() {
        // given
        Long userId = 9L;
        UserIdAndRoleDto user = new UserIdAndRoleDto(userId, Role.MENTOR);

        when(userRepository.findAllByIsDeletedFalse()).thenReturn(List.of(user));
        when(followingService.countFollowers(userId)).thenReturn(1000L);

        // when
        roleRefreshScheduler.refreshRole();

        // then
        verify(userService, never()).updateUserRole(any());
    }

    @Test
    @DisplayName("알림 전송 시 시스템 발신자 ID는 -1")
    void refreshRole_useSystemSenderId() {
        // given
        Long userId = 10L;
        UserIdAndRoleDto user = new UserIdAndRoleDto(userId, Role.USER);

        when(userRepository.findAllByIsDeletedFalse()).thenReturn(List.of(user));
        when(followingService.countFollowers(userId)).thenReturn(100L);
        doNothing().when(userService).updateUserRole(any());
        doNothing().when(notificationSendService).sendNotification(any());

        // when
        roleRefreshScheduler.refreshRole();

        // then
        verify(notificationSendService).sendNotification(argThat(request ->
                request.getSenderId().equals(-1L) &&
                        request.getSenderNickname().equals("시스템")
        ));
    }
}
