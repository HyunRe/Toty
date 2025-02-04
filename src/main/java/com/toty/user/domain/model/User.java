package com.toty.user.domain.model;


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
public class User {

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
    private SubscribeInfo subscribeInfo;

    @Column(name = "status_message")
    private String statusMessage;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public User(String email, String password, String nickname, String phoneNumber, LoginProvider loginProvider) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.loginProvider = loginProvider;
    }

    public void updateInfo(UserInfoUpdateRequest newInfo, String imgPath) {
        this.nickname = newInfo.getNickname();
        this.profileImageUrl = imgPath;
        this.subscribeInfo = new SubscribeInfo(newInfo.isEmailSubscribed(), newInfo.isSmsSubscribed());
        this.statusMessage = newInfo.getStatusMessage();
        this.phoneNumber = newInfo.getPhoneNumber();
    }

    public void deleteUser() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
