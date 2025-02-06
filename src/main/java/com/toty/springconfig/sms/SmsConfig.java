package com.toty.springconfig.sms;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class SmsConfig {

    @Value("${spring.coolsms.api.fromnumber}")
    private String messageFrom;

    @Value("${spring.coolsms.api.key}")
    private String apiKey;

    @Value("${spring.coolsms.api.secret}")
    private String apiSecretKey;
}
