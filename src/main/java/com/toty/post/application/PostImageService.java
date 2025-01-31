package com.toty.post.application;

import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostImage;
import com.toty.post.domain.repository.PostImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostImageService {
    private final PostImageRepository postImageRepository;

    // 이미지 업로드
    public void uploadImage(Post post, List<MultipartFile> images) throws IOException {
        // 이미지 파일 저장을 위한 경로 설정
        String uploadsDir = "src/main/resources/static/posts/images";

        // 각 이미지 파일에 대해 업로드 및 DB 저장 수행
        for (MultipartFile image : images) {
            // 이미지 파일 경로를 저장
            String dbFilePath = saveImage(image, uploadsDir);
            // ProductThumbnail 엔티티 생성 및 저장
            PostImage postImage = new PostImage(post, dbFilePath);
            // 관계 설정
            post.addPostImage(postImage);
            // DB 저장
            postImageRepository.save(postImage);
        }
    }

    // 이미지 수정 & 삭제
    public void updateImages(Post post, List<MultipartFile> newImages) throws IOException {
        // 기존 이미지 목록 가져오기
        List<PostImage> existingImages = post.getPostImages();

        // 새 이미지 업로드 및 관계 설정
        for (MultipartFile imageFile : newImages) {
            String dbFilePath = saveImage(imageFile, "src/main/resources/static/posts/images");

            boolean isDuplicate = existingImages.stream()
                    .anyMatch(existingImage -> existingImage.getImageUrl().equals(dbFilePath));

            if (!isDuplicate) {
                PostImage postImage = new PostImage(post, dbFilePath);
                post.addPostImage(postImage); // 관계 설정
                postImageRepository.save(postImage); // DB 저장
            }
        }

        // 삭제된 이미지 처리
        existingImages.removeIf(existingImage -> {
            boolean isRemoved = newImages.stream()
                    .map(imageFile -> {
                        try {
                            return saveImage(imageFile, "src/main/resources/static/posts/images");
                        } catch (IOException e) {
                            throw new IllegalStateException("이미지 저장 중 오류 발생", e);
                        }
                    })
                    .noneMatch(newImagePath -> newImagePath.equals(existingImage.getImageUrl()));

            if (isRemoved) {
                postImageRepository.delete(existingImage); // DB에서 삭제
            }

            return isRemoved;
        });
    }

    // 이미지 저장
    public String saveImage(MultipartFile image, String uploadsDir) throws IOException {
        // 파일 이름 생성
        String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + image.getOriginalFilename();
        // 실제 파일이 저장될 경로
        String filePath = uploadsDir + fileName;
        // DB에 저장할 경로 문자열
        String dbFilePath = "/posts/images/" + fileName;

        Path path = Paths.get(filePath); // Path 객체 생성
        Files.createDirectories(path.getParent()); // 디렉토리 생성
        Files.write(path, image.getBytes()); // 디렉토리에 파일 저장

        return dbFilePath;
    }
}
