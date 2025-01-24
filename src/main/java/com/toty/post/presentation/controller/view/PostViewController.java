package com.toty.post.presentation.controller.view;

import com.toty.base.pagination.PaginationResult;
import com.toty.base.response.SuccessResponse;
import com.toty.post.application.PostService;
import com.toty.post.presentation.dto.response.postdetail.PostDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/view/posts")
@RequiredArgsConstructor
public class PostViewController {
    private final PostService postService;

    // 전체 게시글 목록 조회
    @GetMapping("/list")
    public String postList(@RequestParam(name = "page", defaultValue = "1") int page,
                           @RequestParam(name = "filter", required = false) String filter,
                           Model model) {
        PaginationResult result = postService.getPagedPosts(page, filter);
        model.addAttribute("result", result);

        return "post/list";
    }

    // 내가 작성한 게시글 목록 조회
    @GetMapping("/myList")
    public String myPostList(@RequestParam(name = "page", defaultValue = "1") int page,
                             @RequestParam("userId") Long userId,
                             @RequestParam(name = "postCategory", required = false) String postCategory,
                             Model model) {
        PaginationResult result = postService.getPagedPostsByUserId(page, userId, postCategory);
        model.addAttribute("result", result);

        return "post/myList";
    }

    // 카테고리 별 목록 조회
    @GetMapping("/categoryList")
    public String postCategoryList(@RequestParam(name = "page", defaultValue = "1") int page,
                                   @RequestParam(name = "postCategory", required = false) String postCategory,
                                   Model model) {
        PaginationResult result = postService.getPagedPostsByCategory(page, postCategory);
        model.addAttribute("result", result);

        return "post/categoryList";
    }

    // 카테고리 별 게시글 상세 보기
    @GetMapping("/{id}/detail")
    public String postDetail(@PathVariable Long id,
                             @RequestParam(name = "page", defaultValue = "1") int page,
                             @RequestParam(name = "likeAction", required = false) String likeAction,
                             @RequestParam(name = "postCategory", required = false) String postCategory,
                             Model model) {
        postService.incrementViewCount(id);
        if ("like".equals(likeAction)) {
            postService.incrementLikeCount(id);
        } else {
            postService.decrementLikeCount(id);
        }

        PostDetailResponse response = postService.getPostDetailByCategory(page, id, postCategory);
        model.addAttribute("result", response);

        return "post/detail";
    }
}
