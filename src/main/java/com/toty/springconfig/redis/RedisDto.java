package com.toty.springconfig.redis;

import java.time.Duration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RedisDto {
    String key;
    Object value;
    Duration duration;
}