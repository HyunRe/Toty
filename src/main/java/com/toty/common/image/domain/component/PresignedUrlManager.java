package com.toty.common.image.domain.component;

import com.toty.common.image.domain.model.Image;
import com.toty.common.image.infrastructure.S3PresignedUrlProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PresignedUrlManager {
    private final S3PresignedUrlProvider s3PresignedUrlProvider;

    /**
     * 이미지 엔티티의 URL이 만료되었는지 확인하고, 필요 시 갱신합니다.
     * (기존 PresignedUrlManagementUtil의 로직 반영)
     */
    @Transactional
    public String getValidUrl(Image image) {
        // 1. URL이 없거나 만료 시간이 지났는지 확인 (여유 시간 5분 제외)
        if (image.getUrl() == null || image.getExpiredAt() == null ||
                image.getExpiredAt().isBefore(LocalDateTime.now().plusMinutes(5))) {

            // 2. 새로운 Presigned URL 생성 (Infrastructure 계층 호출)
            String newUrl = s3PresignedUrlProvider.generatePresignedUrl(image.getPath());

            // 3. 엔티티 상태 업데이트 (만료 시간은 60분으로 설정)
            image.updateUrl(newUrl, LocalDateTime.now().plusMinutes(60));
        }

        return image.getUrl();
    }
}
