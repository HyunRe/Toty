package com.toty.post.presentation.api;

import com.toty.common.pagination.PaginationResult;
import com.toty.base.response.SuccessResponse;
import com.toty.common.annotation.CurrentUser;
import com.toty.post.application.PostLikeService;
import com.toty.post.application.PostPaginationService;
import com.toty.post.application.PostService;
import com.toty.post.domain.model.Post;
import com.toty.post.dto.request.PostCreateRequest;
import com.toty.post.dto.request.PostUpdateRequest;
import com.toty.post.dto.response.postdetail.PostDetailResponse;
import com.toty.user.domain.model.User;
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
    private final PostPaginationService postPaginationService;
    private final PostLikeService postLikeService;

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@CurrentUser User user,
                                             @PathVariable Long id) {
        postService.deletePost(user, id);
        return ResponseEntity.ok("true");
    }

    // 게시글 좋아요 토글 (증감소)
    @PatchMapping("/{id}/like")
    public ResponseEntity<Boolean> toggleLike(@PathVariable Long id,
                                              @CurrentUser User user,
                                              @RequestParam(name = "likeAction", required = false) String likeAction) {
        Boolean isLiked = postLikeService.toggleLikeAction(id, user.getId(), likeAction);
        return ResponseEntity.ok(isLiked);
    }

    // 이 밑은 테스트 용도

    // 게시글 작성 (test)
    @PostMapping("/create")
    public ResponseEntity<SuccessResponse> createPost(@RequestParam("userId") Long userId,
                                                      @Valid @RequestBody PostCreateRequest postCreateRequest) {
        Post post = postService.createPost(userId, postCreateRequest);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "게시글 생성 성공",
                post
        );

        return ResponseEntity.ok(successResponse);
    }

    // 게시글 수정 (test)
    @PatchMapping("/{id}")
    public ResponseEntity<SuccessResponse> updatePost(@CurrentUser User user,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody PostUpdateRequest postUpdateRequest) {
        Post post = postService.updatePost(user, id, postUpdateRequest);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "게시글 수정 성공",
                post
        );

        return ResponseEntity.ok(successResponse);
    }

    // 전체 게시글 목록 조회 (test)
    @GetMapping("/list")
    public ResponseEntity<SuccessResponse> postList(@RequestParam(name = "page", defaultValue = "1") int page,
                                                    @RequestParam(name = "filter", required = false) String filter) {
        PaginationResult result = postPaginationService.getPagedPosts(page, filter);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "전체 게시글 목록 조회",
                result.getContent().size()
        );

        return ResponseEntity.ok(successResponse);
    }

    // 내가 작성한 게시글 목록 조회 (test)
    @GetMapping("/myList")
    public ResponseEntity<SuccessResponse> myPostList(@CurrentUser User user,
                                                      @RequestParam(name = "page", defaultValue = "1") int page,
                                                      @RequestParam(name = "postCategory", required = false) String postCategory) {
        PaginationResult result = postPaginationService.getPagedPostsByUserId(page, user.getId(), postCategory);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "내가 작성한 게시글 목록 조회",
                result.getContent().size()
        );

        return ResponseEntity.ok(successResponse);
    }

    // 카테고리 별 목록 조회 (test)
    @GetMapping("/categoryList")
    public ResponseEntity<SuccessResponse> postCategoryList(@RequestParam(name = "page", defaultValue = "1") int page,
                                                            @RequestParam(name = "postCategory", required = false) String postCategory) {
        PaginationResult result = postPaginationService.getPagedPostsByCategory(page, postCategory);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "카테고리 별 목록 조회",
                result.getContent().size()
        );

        return ResponseEntity.ok(successResponse);
    }

    // 카테고리 별 게시글 상세 보기 (test)
    @GetMapping("/{id}/detail")
    public ResponseEntity<SuccessResponse> postDetail(@PathVariable Long id,
                                                      @RequestParam(name = "page", defaultValue = "1") int page,
                                                      @RequestParam(name = "postCategory", required = false) String postCategory) {
        postService.incrementViewCount(id);

        PostDetailResponse response = postPaginationService.getPostDetailByCategory(page, id, postCategory);
        SuccessResponse successResponse = new SuccessResponse(
                HttpStatus.OK.value(),
                "카테고리 별 게시글 상세 보기",
                response
        );

        return ResponseEntity.ok(successResponse);
    }
}
