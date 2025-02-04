package com.toty.user.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SubscribeInfo {

    @Column(name = "email_subscribed", columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean emailSubscribed = false;

    @Column(name = "sms_subscribed", columnDefinition = "TINYINT(1)  DEFAULT 0")
    private boolean smsSubscribed = false;

    @Column(name = "notification", columnDefinition = "TINYINT(1)  DEFAULT 0")
    private boolean notification = false;

    public SubscribeInfo(boolean emailSubscribed, boolean smsSubscribed, boolean notification) {
        this.emailSubscribed = emailSubscribed;
        this.smsSubscribed = smsSubscribed;
        this.notification = notification;
    }
}
