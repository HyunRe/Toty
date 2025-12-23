package com.toty.user.domain.model;

import com.toty.common.domain.BaseTime;
import com.toty.user.dto.request.UserInfoUpdateRequest;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "username")
    private String username;

    @Column(name = "nickname")
    private String nickname; // 필수값

    @Column(name = "phone_number")
    private String phoneNumber; // 폼 로그인 시 필수값

    @Column(name = "profile_image")
    private String profileImageUrl;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private LoginProvider loginProvider;

    @Embedded
    private UserSubscribeInfo userSubscribeInfo;

    @Column(name = "status_message")
    private String statusMessage;

    @Column(name = "is_deleted", columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public User(String email, String password, String nickname, String phoneNumber, LoginProvider loginProvider, boolean isDeleted, String username,
           boolean smsSubscribed, boolean emailSubscribed, boolean notificationAllowed) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.loginProvider = loginProvider;
        this.username = username;
        this.isDeleted = isDeleted;
        this.userSubscribeInfo = new UserSubscribeInfo(smsSubscribed, emailSubscribed, notificationAllowed);
    }

    public void updateInfo(UserInfoUpdateRequest newInfo, String imgPath) {
        this.nickname = newInfo.getNickname();
        this.profileImageUrl = imgPath;
        this.userSubscribeInfo = new UserSubscribeInfo(newInfo.isEmailSubscribed(), newInfo.isSmsSubscribed(), newInfo.isNotificationAllowed());
        this.statusMessage = newInfo.getStatusMessage();
        this.phoneNumber = newInfo.getPhoneNumber();
    }

    public void deleteUser() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void updateRole(Role role) {
        this.role = role;
    };

    // 구독 변경
    public void updateEmailSubscription(boolean emailSubscribed) {
        this.userSubscribeInfo = this.userSubscribeInfo.updateEmailSubscription(emailSubscribed);
    }

    public void updateSmsSubscription(boolean smsSubscribed) {
        this.userSubscribeInfo = this.userSubscribeInfo.updateSmsSubscription(smsSubscribed);
    }

    public void updateNotificationAllowed(boolean notificationAllowed) {
        this.userSubscribeInfo = this.userSubscribeInfo.updateNotificationAllowed(notificationAllowed);
    }

    // 이미지 변경
    public void updateprofileImg(String imgPath) {
        this.profileImageUrl = imgPath;
    }

    // 휴대폰 번호 변경
    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // 닉네임 변경
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 비밀번호 변경
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void saveStatusMessage(String request) { this.statusMessage = request; }
}

