package com.toty.user.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserSubscribeInfo {
    @Column(name = "email_subscribed", columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean emailSubscribed = false;

    @Column(name = "sms_subscribed", columnDefinition = "TINYINT(1)  DEFAULT 0")
    private boolean smsSubscribed = false;

    @Column(name = "notification", columnDefinition = "TINYINT(1)  DEFAULT 0")
    private boolean notificationAllowed = false;

    public UserSubscribeInfo(boolean emailSubscribed, boolean smsSubscribed, boolean notificationAllowed) {
        this.emailSubscribed = emailSubscribed;
        this.smsSubscribed = smsSubscribed;
        this.notificationAllowed = notificationAllowed;
    }

    // 구독 변경
    public UserSubscribeInfo updateEmailSubscription(boolean emailSubscribed) {
        return new UserSubscribeInfo(emailSubscribed, this.smsSubscribed, this.notificationAllowed);
    }

    public UserSubscribeInfo updateSmsSubscription(boolean smsSubscribed) {
        return new UserSubscribeInfo(this.emailSubscribed, smsSubscribed, this.notificationAllowed);
    }

    public UserSubscribeInfo updateNotificationAllowed(boolean notificationAllowed) {
        return new UserSubscribeInfo(this.emailSubscribed, this.smsSubscribed, notificationAllowed);
    }
}
