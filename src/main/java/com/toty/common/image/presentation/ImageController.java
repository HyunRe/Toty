package com.toty.common.image.presentation;

import com.toty.common.annotation.CurrentUser;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.common.image.application.ImageUploadService;
import com.toty.common.image.dto.ImageResponse;
import com.toty.user.domain.model.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Image", description = "이미지 API")
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageUploadService imageUploadService;

    /**
     * Summernote 에디터에서 이미지 업로드 시 호출되는 API
     */
    @PostMapping("/summernote")
    public ResponseEntity<ImageResponse> uploadSummernoteImage(@CurrentUser User user,
                                                               @RequestParam("file") MultipartFile file) {
        // Application 서비스를 호출하여 업로드 진행 및 URL 획득
        String uploadedUrl = imageUploadService.uploadForSummernote(user.getId(), file);

        // DTO에 담아서 JSON 형태로 반환
        return ResponseEntity.ok(ImageResponse.of(uploadedUrl));
    }
}
