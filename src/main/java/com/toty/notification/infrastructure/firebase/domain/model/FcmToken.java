package com.toty.notification.infrastructure.firebase.domain.model;

import com.toty.common.domain.BaseTime;
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

    @Column(name = "token", nullable = false, length = 500)
    private String token;

    @Column(name = "is_active", columnDefinition = "TINYINT(1) DEFAULT 1", nullable = false)
    private boolean isActive = true;

    public FcmToken(User user, String token) {
        this.user = user;
        this.token = token;
    }

    // 토큰 값 재활성화
    public void activate(String newToken) {
        this.token = newToken;
        this.isActive = true;
    }

    // 토큰 값 비활성화
    public void deactivate() {
        this.isActive = false;
    }
}
