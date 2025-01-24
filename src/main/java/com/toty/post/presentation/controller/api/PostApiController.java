package com.toty.post.presentation.controller.api;

import com.toty.base.pagination.PaginationResult;
import com.toty.base.response.SuccessResponse;
import com.toty.post.application.PostService;
import com.toty.post.domain.model.Post;
import com.toty.post.presentation.dto.request.PostCreateRequest;
import com.toty.post.presentation.dto.request.PostUpdateRequest;
import com.toty.post.presentation.dto.response.postdetail.PostDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostApiController {
    private final PostService postService;

    // 게시글 작성
    @PostMapping("/create")
    public ResponseEntity<?> createPost(@RequestParam("userId") Long userId,
                                        @Valid @RequestBody PostCreateRequest postCreateRequest) {
        Post post = postService.createPost(userId, postCreateRequest);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "게시글 생성 성공",
                post
        );

        return ResponseEntity.ok(successResponse);
    }

    // 게시글 수정
    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id,
                                        @Valid @RequestBody PostUpdateRequest postUpdateRequest) {
        Post post = postService.updatePost(id, postUpdateRequest);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "게시글 수정 성공",
                post
        );

        return ResponseEntity.ok(successResponse);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "게시글 삭제 성공",
                null
        );

        return ResponseEntity.ok(successResponse);
    }

    // 전체 게시글 목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> postList(@RequestParam(name = "page", defaultValue = "1") int page,
                                      @RequestParam(name = "filter", required = false) String filter) {
        PaginationResult result = postService.getPagedPosts(page, filter);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "전체 게시글 목록 조회",
                null
        );

        return ResponseEntity.ok(successResponse);
    }

    // 내가 작성한 게시글 목록 조회
    @GetMapping("/myList")
    public ResponseEntity<?> myPostList(@RequestParam(name = "page", defaultValue = "1") int page,
                                        @RequestParam("userId") Long userId,
                                        @RequestParam(name = "postCategory", required = false) String postCategory) {
        PaginationResult result = postService.getPagedPostsByUserId(page, userId, postCategory);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "내가 작성한 게시글 목록 조회",
                null
        );

        return ResponseEntity.ok(successResponse);
    }

    // 카테고리 별 목록 조회
    @GetMapping("/categoryList")
    public ResponseEntity<?> postCategoryList(@RequestParam(name = "page", defaultValue = "1") int page,
                                              @RequestParam(name = "postCategory", required = false) String postCategory) {
        PaginationResult result = postService.getPagedPostsByCategory(page, postCategory);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "카테고리 별 목록 조회",
                null
        );

        return ResponseEntity.ok(successResponse);
    }

    // 카테고리 별 게시글 상세 보기
    @GetMapping("/{id}/detail")
    public ResponseEntity<?> postDetail(@PathVariable Long id,
                                        @RequestParam(name = "page", defaultValue = "1") int page,
                                        @RequestParam(name = "likeAction", required = false) String likeAction,
                                        @RequestParam(name = "postCategory", required = false) String postCategory) {
        postService.incrementViewCount(id);
        if ("like".equals(likeAction)) {
            postService.incrementLikeCount(id);
        } else {
            postService.decrementLikeCount(id);
        }

        PostDetailResponse response = postService.getPostDetailByCategory(page, id, postCategory);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "카테고리 별 게시글 상세 보기",
                response
        );

        return ResponseEntity.ok(successResponse);
    }
}
