package com.toty.post.presentation.post.api;

import com.toty.common.pagination.PaginationResult;
import com.toty.common.annotation.CurrentUser;
import com.toty.post.application.postService.PostImageService;
import com.toty.post.application.postService.PostLikeService;
import com.toty.post.application.postService.PostLikePaginationService;
import com.toty.post.application.postService.PostPaginationService;
import com.toty.post.application.postService.PostService;
import com.toty.post.domain.model.post.Post;
import com.toty.post.dto.request.PostLikeActionRequest;
import com.toty.post.dto.request.PostCreateRequest;
import com.toty.post.dto.request.PostUpdateRequest;
import com.toty.post.dto.request.PostScrapeRequest;
import com.toty.post.dto.response.postdetail.PostDetailResponse;
import com.toty.user.application.UserScrapeService;
import com.toty.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Post", description = "게시글 API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostApiController {
    private final PostService postService;
    private final PostPaginationService postPaginationService;
    private final PostLikePaginationService postLikePaginationService;
    private final PostLikeService postLikeService;
    private final PostImageService postImageService;
    private final UserScrapeService userScrapeService;

    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@CurrentUser User user,
                                             @PathVariable Long id) {
        postService.deletePost(user, id);
        return ResponseEntity.ok("true");
    }

    @Operation(summary = "게시글 좋아요 토글", description = "게시글에 좋아요를 추가하거나 취소합니다")
    @PatchMapping("/{id}/like")
    public ResponseEntity<Integer> toggleLike(@PathVariable Long id,
                                              @CurrentUser User user,
                                              @RequestBody PostLikeActionRequest postLikeActionRequest) {
        int likeCount = postLikeService.toggleLikeAction(id, user.getId(), postLikeActionRequest.getLikeAction());
        return ResponseEntity.ok(likeCount);
    }

    @Operation(summary = "좋아요 상태 조회", description = "특정 게시글에 대한 로그인한 사용자의 좋아요 여부를 조회합니다")
    @GetMapping("/{id}/like-status")
    public ResponseEntity<Boolean> getLikeStatus(@PathVariable Long id,
                                                 @CurrentUser User user) {
        boolean isLiked = postLikeService.isPostLikedByUser(id, user.getId());
        return ResponseEntity.ok(isLiked);
    }

    @Operation(summary = "게시글 스크랩 토글", description = "게시글을 스크랩하거나 스크랩 취소합니다")
    @PatchMapping("/{id}/scrape")
    public ResponseEntity<String> toggleScrape(@PathVariable Long id,
                                                @CurrentUser User user,
                                                @RequestBody PostScrapeRequest postScrapeRequest) {
        String scrape = userScrapeService.toggleScrape(id, user.getId(), postScrapeRequest.getScrape());
        return ResponseEntity.ok(scrape);
    }

    @Operation(summary = "스크랩 상태 조회", description = "특정 게시글에 대한 로그인한 사용자의 스크랩 여부를 조회합니다")
    @GetMapping("/{id}/scrape-status")
    public ResponseEntity<Boolean> getScrapeStatus(@PathVariable Long id,
                                                   @CurrentUser User user) {
        boolean isScraped = userScrapeService.isPostScrapedByUser(id, user.getId());
        return ResponseEntity.ok(isScraped);
    }

    @Operation(summary = "이미지 업로드", description = "게시글에 첨부할 이미지를 업로드합니다")
    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String fileUrl = postImageService.saveImage(file, "uploads/posts/images");
        return ResponseEntity.ok(fileUrl);
    }

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다")
    @PostMapping("/create")
    public ResponseEntity<Post> createPost(@CurrentUser User user,
                                           @RequestBody @Valid PostCreateRequest postCreateRequest) {
        Post post = postService.createPost(user, postCreateRequest);
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "게시글 수정", description = "특정 게시글을 수정합니다")
    @PatchMapping("/{id}")
    public ResponseEntity<Post> updatePost(@CurrentUser User user,
                                           @PathVariable Long id,
                                           @RequestBody @Valid PostUpdateRequest postUpdateRequest) {
        Post post = postService.updatePost(user, id, postUpdateRequest);
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "전체 게시글 목록 조회", description = "전체 게시글 목록을 페이지네이션으로 조회합니다")
    @GetMapping("/list")
    public ResponseEntity<PaginationResult> postList(@RequestParam(name = "page", defaultValue = "1") int page,
                                                     @RequestParam(name = "sort", required = false) String sort) {
        PaginationResult result = postPaginationService.getPagedPosts(page, sort);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "내가 작성한 게시글 목록 조회", description = "로그인한 사용자가 작성한 게시글 목록을 페이지네이션으로 조회합니다")
    @GetMapping("/myList")
    public ResponseEntity<PaginationResult> myPostList(@CurrentUser User user,
                                                       @RequestParam(name = "page", defaultValue = "1") int page,
                                                       @RequestParam(name = "postCategory", required = false) String postCategory) {
        PaginationResult result = postPaginationService.getPagedPostsByUserId(page, user.getId(), postCategory);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "카테고리별 게시글 목록 조회", description = "특정 카테고리의 게시글 목록을 페이지네이션으로 조회합니다")
    @GetMapping("/categoryList")
    public ResponseEntity<PaginationResult> postCategoryList(@RequestParam(name = "page", defaultValue = "1") int page,
                                                             @RequestParam(name = "postCategory", required = false) String postCategory) {
        PaginationResult result = postPaginationService.getPagedPostsByCategory(page, postCategory);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "좋아요한 게시글 목록 조회", description = "로그인한 사용자가 좋아요한 게시글 목록을 페이지네이션으로 조회합니다")
    @GetMapping("/myLikeList")
    public ResponseEntity<PaginationResult> myLikePostList(@CurrentUser User user,
                                                           @RequestParam(name = "page", defaultValue = "1") int page,
                                                           @RequestParam(name = "postCategory", defaultValue = "GENERAL") String postCategory) {
        PaginationResult result = postLikePaginationService.getLikedPostsByUserId(page, user.getId(), postCategory);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회하고 조회수를 증가시킵니다")
    @GetMapping("/{id}/detail")
    public ResponseEntity<PostDetailResponse> postDetail(@CurrentUser User user,
                                                         @PathVariable Long id,
                                                         @RequestParam(name = "page", defaultValue = "1") int page,
                                                         @RequestParam(name = "postCategory", required = false) String postCategory) {
        postService.incrementViewCount(id);
        PostDetailResponse response = postPaginationService.getPostDetailByCategory(page, id, postCategory, user.getId());
        return ResponseEntity.ok(response);
    }
}
