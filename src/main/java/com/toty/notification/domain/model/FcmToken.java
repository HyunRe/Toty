package com.toty.notification.domain.model;

import com.toty.base.domain.model.BaseTime;
import com.toty.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fcm_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    public FcmToken(User user, String token) {
        this.user = user;
        this.token = token;
    }

    // 토큰 값 업데이트
    public void updateToken(String newToken) {
        this.token = newToken;
    }
}
