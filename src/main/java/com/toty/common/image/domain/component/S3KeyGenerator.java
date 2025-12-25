package com.toty.common.image.domain.component;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class S3KeyGenerator {
    public String generateKey(String domain, Long id, String originalName) {
        String ext = originalName.substring(originalName.lastIndexOf("."));
        return "images/" + domain + "/" + id + "/" + UUID.randomUUID() + ext;
    }
}
