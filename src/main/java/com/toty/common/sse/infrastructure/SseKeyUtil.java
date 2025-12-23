package com.toty.common.sse.infrastructure;

public class SseKeyUtil {
    public static String commentKey(String domain, Long postId) { return domain + ":comment:post:" + postId; }

    public static String postLikeKey(String domain, Long postId) {
        return domain + ":like:post:" + postId;
    }

    public static String notificationKey(String domain, Long userId) {
        return domain + ":user:" + userId;
    }
}
