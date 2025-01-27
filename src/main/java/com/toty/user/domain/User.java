package com.toty.user.domain;


import com.toty.user.presentation.dto.request.UserInfoUpdateRequest;
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

    @Column
    private String phoneNumber; // 폼 로그인 시 필수값

    @Column(nullable = false)
    private String nickname; // 필수값

    @Column
    private String profileImageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoginProvider loginProvider;

    @Column(nullable = false, columnDefinition = "TINYINT(1) default 0")
    private boolean emailSubscribed = false;

    @Column(nullable = false, columnDefinition = "TINYINT(1) default 0")
    private boolean smsSubscribed = false;

    @Column
    private String statusMessage;

    @Column(nullable = false, columnDefinition = "TINYINT(1) default 0")
    private boolean deleted = false;

    @Column
    private LocalDateTime deletedAt;

    @Builder
    public User(String email, String password, String nickname, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
    }

    public void updateInfo(UserInfoUpdateRequest newInfo, String imgPath) {
        this.nickname = newInfo.getNickname();
        this.profileImageUrl = imgPath;
        this.emailSubscribed = newInfo.isEmailSubscribed();
        this.smsSubscribed = newInfo.isSmsSubscribed();
        this.statusMessage = newInfo.getStatusMessage();
        this.phoneNumber = newInfo.getPhoneNumber();
    }
}
