package com.toty.common.image.infrastructure;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final S3Template s3Template;

    public String uploadFile(String key, MultipartFile file) {
        try {
            var s3Resource = s3Template.upload(bucket, key, file.getInputStream());
            return s3Resource.getURL().toString();
        } catch (IOException e) {
            throw new ExpectedException(ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    /**
     * S3에서 파일을 비동기로 삭제합니다.
     * @param key S3 객체 키
     */
    @Async("taskExecutor")
    public void deleteFile(String key) {
        try {
            log.info("S3 파일 비동기 삭제 시작 - key: {}", key);
            s3Template.deleteObject(bucket, key);
            log.info("S3 파일 비동기 삭제 완료 - key: {}", key);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패 - key: {}, error: {}", key, e.getMessage(), e);
        }
    }
}
