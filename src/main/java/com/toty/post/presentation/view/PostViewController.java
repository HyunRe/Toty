package com.toty.post.presentation.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toty.common.pagination.PaginationResult;
import com.toty.common.annotation.CurrentUser;
import com.toty.post.application.PostLikeService;
import com.toty.post.application.PostPaginationService;
import com.toty.post.application.PostService;
import com.toty.post.application.PostTagService;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostTag;
import com.toty.post.dto.response.postdetail.PostDetailResponse;
import com.toty.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/view/posts")
@RequiredArgsConstructor
public class PostViewController {
    private final PostService postService;
    private final PostPaginationService postPaginationService;
    private final PostLikeService postLikeService;
    private final PostTagService postTagService;

    @GetMapping("/create")
    public String createPost() {
        return "post/create";
    }

    @GetMapping("/update/{id}")
    public String updatePost(@PathVariable Long id, Model model) {
        Post post = postService.findPostById(id);
        model.addAttribute("post", post);
        return "post/update";
    }

    // 전체 게시글 목록 조회
    @GetMapping("/list")
    public String postList(@RequestParam(name = "page", defaultValue = "1") int page,
                           @RequestParam(name = "sort", required = false) String sort,
                           Model model) {
        PaginationResult post = postPaginationService.getPagedPosts(page, sort);
        model.addAttribute("post", post);
        model.addAttribute("sort", sort);
        model.addAttribute("page", page);
        return "post/list";
    }

    // 내가 작성한 게시글 목록 조회
    @GetMapping("/myList")
    public String myPostList(@CurrentUser User user,
                             @RequestParam(name = "page", defaultValue = "1") int page,
                             @RequestParam(name = "postCategory", required = false) String postCategory,
                             Model model) {
        PaginationResult post = postPaginationService.getPagedPostsByUserId(page, user.getId(), postCategory);
        model.addAttribute("post", post);
        model.addAttribute("postCategory", postCategory);
        model.addAttribute("page", page);
        return "user/myList";
    }

    // 카테고리 별 목록 조회
    @GetMapping("/categoryList")
    public String postCategoryList(@RequestParam(name = "page", defaultValue = "1") int page,
                                   @RequestParam(name = "postCategory", required = false) String postCategory,
                                   Model model) {
        PaginationResult post = postPaginationService.getPagedPostsByCategory(page, postCategory);
        model.addAttribute("post", post);
        model.addAttribute("postCategory", postCategory);
        return "post/categoryList";
    }

    // 카테고리 별 게시글 상세 보기
    @GetMapping("/{id}/detail")
    public String postDetail(@PathVariable Long id,
                             @CurrentUser User user,
                             @RequestParam(name = "page", defaultValue = "1") int page,
                             @RequestParam(name = "likeAction", required = false) String likeAction,
                             @RequestParam(name = "postCategory", required = false) String postCategory,
                             Model model) throws JsonProcessingException {
        postService.incrementViewCount(id);
        PostDetailResponse response = postPaginationService.getPostDetailByCategory(page, id, postCategory);
        int likeCount = postLikeService.toggleLikeAction(id, user.getId(), likeAction);
        Post post = postService.findPostById(id);
        List<PostTag> postTags = postTagService.getTagsByPost(post);
        List<Map<String, String>> tagList = postTags.stream()
                .map(tag -> Map.of("name", tag.getTagName().toString()))
                .collect(Collectors.toList());
        model.addAttribute("response", response);
        model.addAttribute("postTags", new ObjectMapper().writeValueAsString(tagList)); // JSON 변환
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("postCategory", postCategory);
        return "post/detail";
    }

    // 기본 페이지
    @GetMapping("/home")
    public String home(){
        return "common/home"; // todo
    }

    // 메세지 띄우기
    @PostMapping("/alert")
    public String alert() {
        return "common/alertMsg";
    }

    // 삭제 확인 화면 표시
    @GetMapping("/confirm-delete/{id}")
    public String confirmDelete(@PathVariable Long id, Model model) {
        Post post = postService.findPostById(id);
        model.addAttribute("post", post);
        return "common/dialogMsg";
    }
}
