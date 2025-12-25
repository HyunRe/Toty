package com.toty.common.image.domain.component;


import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.common.image.domain.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageValidator {
    private final ImageRepository imageRepository;

    public void validateImageCount(Long userId, int uploadCount) {
        if (imageRepository.countByUserId(userId) + uploadCount > 10) {
            throw new ExpectedException(ErrorCode.IMAGE_COUNT_LIMIT_EXCEEDED);
        }
    }
}
