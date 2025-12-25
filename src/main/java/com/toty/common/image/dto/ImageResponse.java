package com.toty.common.image.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageResponse {
    private String url;      // S3에 저장된 이미지의 접근 경로
    private String fileName; // 원본 파일명 (필요 시)

    public static ImageResponse of(String url) {
        return ImageResponse.builder()
                .url(url)
                .build();
    }
}
