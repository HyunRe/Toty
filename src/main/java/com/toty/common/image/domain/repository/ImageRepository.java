package com.toty.common.image.domain.repository;

import com.toty.common.image.domain.model.Image;
import com.toty.common.image.domain.model.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    /**
     * 특정 사용자가 업로드한 전체 이미지 개수를 조회합니다.
     * (예: ImageValidator에서 업로드 제한 개수 체크 시 사용)
     */
    long countByUserId(Long userId);

    /**
     * 특정 사용자가 업로드한 모든 이미지 리스트를 조회합니다.
     */
    List<Image> findByUserId(Long userId);

    /**
     * 특정 사용자의 이미지 중, 특정 타입(POST, PROFILE 등)에 해당하는 리스트를 조회합니다.
     * (예: 특정 유저가 게시글에 올린 이미지들만 따로 모아볼 때 사용)
     */
    List<Image> findByUserIdAndType(Long userId, ImageType type);

    /**
     * 특정 사용자의 '프로필 타입' 이미지를 단건 조회합니다.
     * (주로 유저 정보 조회 시 프로필 사진 경로를 가져오기 위해 사용)
     */
    Optional<Image> findByUserIdAndType_Profile(Long userId);

    /**
     * 특정 게시글(Post)에 포함된 이미지 리스트를 조회합니다.
     * (게시글 상세 보기 시 첨부된 이미지들을 불러올 때 사용)
     */
    List<Image> findByPostId(Long postId);

    /**
     * 특정 사용자의 특정 타입 이미지를 모두 삭제합니다.
     * (예: 프로필 사진 변경 시 기존 프로필 이미지 기록을 DB에서 지울 때 사용)
     */
    void deleteByUserIdAndType(Long userId, ImageType type);
}
