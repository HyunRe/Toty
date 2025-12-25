package com.toty.common.image.infrastructure;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    public void deleteFile(String key) {
        s3Template.deleteObject(bucket, key);
    }
}
