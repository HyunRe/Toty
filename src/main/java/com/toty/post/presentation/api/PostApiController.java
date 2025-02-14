package com.toty.post.presentation.api;

import com.toty.common.pagination.PaginationResult;
import com.toty.common.annotation.CurrentUser;
import com.toty.post.application.PostImageService;
import com.toty.post.application.PostLikeService;
import com.toty.post.application.PostPaginationService;
import com.toty.post.application.PostService;
import com.toty.post.domain.model.Post;
import com.toty.post.dto.request.LikeActionRequest;
import com.toty.post.dto.request.PostCreateRequest;
import com.toty.post.dto.request.PostUpdateRequest;
import com.toty.post.dto.request.ScrapeRequest;
import com.toty.post.dto.response.postdetail.PostDetailResponse;
import com.toty.user.application.UserScrapeService;
import com.toty.user.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostApiController {
    private final PostService postService;
    private final PostPaginationService postPaginationService;
    private final PostLikeService postLikeService;
    private final PostImageService postImageService;
    private final UserScrapeService userScrapeService;

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@CurrentUser User user,
                                             @PathVariable Long id) {
        postService.deletePost(user, id);
        return ResponseEntity.ok("true");
    }

    // 게시글 좋아요 토글 (증감소)
    @PatchMapping("/{id}/like")
    public ResponseEntity<Integer> toggleLike(@PathVariable Long id,
                                              @CurrentUser User user,
                                              @RequestBody LikeActionRequest likeActionRequest) {
        int likeCount = postLikeService.toggleLikeAction(id, user.getId(), likeActionRequest.getLikeAction());
        return ResponseEntity.ok(likeCount);
    }

    @GetMapping("/{id}/like-status")
    public ResponseEntity<Boolean> getLikeStatus(@PathVariable Long id,
                                                 @CurrentUser User user) {
        boolean isLiked = postLikeService.isPostLikedByUser(id, user.getId());
        return ResponseEntity.ok(isLiked);
    }

    // 게시글 스크랩 토글
    @PatchMapping("/{id}/scrape")
    public ResponseEntity<String> toggleScrape(@PathVariable Long id,
                                                @CurrentUser User user,
                                                @RequestBody ScrapeRequest scrapeRequest) {
        String scrape = userScrapeService.toggleScrape(id, user.getId(), scrapeRequest.getScrape());
        return ResponseEntity.ok(scrape);
    }

    @GetMapping("/{id}/scrape-status")
    public ResponseEntity<Boolean> getScrapeStatus(@PathVariable Long id,
                                                   @CurrentUser User user) {
        boolean isScraped = userScrapeService.isPostScrapedByUser(id, user.getId());
        return ResponseEntity.ok(isScraped);
    }

    // 이미지 업로드
    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String fileUrl = postImageService.saveImage(file, "uploads/posts/images");
        return ResponseEntity.ok(fileUrl);
    }

    // 게시글 작성
    @PostMapping("/create")
    public ResponseEntity<Post> createPost(@CurrentUser User user,
                                           @RequestBody @Valid PostCreateRequest postCreateRequest) {
        Post post = postService.createPost(user, postCreateRequest);
        return ResponseEntity.ok(post);
    }

    // 게시글 수정
    @PatchMapping("/{id}")
    public ResponseEntity<Post> updatePost(@CurrentUser User user,
                                           @PathVariable Long id,
                                           @RequestBody @Valid PostUpdateRequest postUpdateRequest) {
        Post post = postService.updatePost(user, id, postUpdateRequest);
        return ResponseEntity.ok(post);
    }

    // 전체 게시글 목록 조회
    @GetMapping("/list")
    public ResponseEntity<PaginationResult> postList(@RequestParam(name = "page", defaultValue = "1") int page,
                                                     @RequestParam(name = "sort", required = false) String sort) {
        PaginationResult result = postPaginationService.getPagedPosts(page, sort);
        return ResponseEntity.ok(result);
    }

    // 내가 작성한 게시글 목록 조회
    @GetMapping("/myList")
    public ResponseEntity<PaginationResult> myPostList(@CurrentUser User user,
                                                       @RequestParam(name = "page", defaultValue = "1") int page,
                                                       @RequestParam(name = "postCategory", required = false) String postCategory) {
        PaginationResult result = postPaginationService.getPagedPostsByUserId(page, user.getId(), postCategory);
        return ResponseEntity.ok(result);
    }

    // 카테고리 별 목록 조회
    @GetMapping("/categoryList")
    public ResponseEntity<PaginationResult> postCategoryList(@RequestParam(name = "page", defaultValue = "1") int page,
                                                             @RequestParam(name = "postCategory", required = false) String postCategory) {
        PaginationResult result = postPaginationService.getPagedPostsByCategory(page, postCategory);
        return ResponseEntity.ok(result);
    }

    // 카테고리 별 게시글 상세 보기
    @GetMapping("/{id}/detail")
    public ResponseEntity<PostDetailResponse> postDetail(@PathVariable Long id,
                                                         @RequestParam(name = "page", defaultValue = "1") int page,
                                                         @RequestParam(name = "postCategory", required = false) String postCategory) {
        postService.incrementViewCount(id);
        PostDetailResponse response = postPaginationService.getPostDetailByCategory(page, id, postCategory);
        return ResponseEntity.ok(response);
    }
}
