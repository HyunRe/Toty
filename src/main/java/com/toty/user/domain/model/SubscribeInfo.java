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

    @Column(name = "email_subscribed", columnDefinition = "TINYINT(0)")
    private boolean emailSubscribed;

    @Column(name = "sms_subscribed", columnDefinition = "TINYINT(0)")
    private boolean smsSubscribed;

    public SubscribeInfo(boolean emailSubscribed, boolean smsSubscribed) {
        this.emailSubscribed = emailSubscribed;
        this.smsSubscribed = smsSubscribed;
    }
}
