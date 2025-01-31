package com.toty.comment.presentation.comtroller.view;

import com.toty.comment.application.CommentService;
import com.toty.comment.presentation.dto.request.CommentCreateUpdateRequest;
import com.toty.post.application.PostService;
import com.toty.post.domain.model.Post;
import com.toty.post.presentation.dto.request.PostUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/view/comments")
@RequiredArgsConstructor
public class CommentViewController {
    private final CommentService commentService;
    private final PostService postService;

    @GetMapping("/create")
    public String createPostForm() {
        return "post/detail";
    }

    @PostMapping("/create")
    public String createPost(@RequestParam("userId") Long userId,
                             @RequestParam("postId") Long postId,
                             @ModelAttribute @Valid CommentCreateUpdateRequest commentCreateUpdateRequest,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "post/detail"; // 유효성 검사 실패 시, 다시 폼을 반환
        }
        commentService.createComment(userId, postId, commentCreateUpdateRequest);
        return "redirect:/view/posts/detail";
    }

    @GetMapping("/update/{id}")
    public String updatePostForm(@PathVariable Long id, Model model) {
        Post post = postService.findPostById(id);
        model.addAttribute("post", post);
        return "post/update";
    }

    @PatchMapping("/{id}")
    public String updatePost(@PathVariable Long id,
                             @ModelAttribute @Valid PostUpdateRequest postUpdateRequest,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "post/update"; // 유효성 검사 실패 시, 다시 폼을 반환
        }
        postService.updatePost(id, postUpdateRequest);
        return "redirect:/view/posts/myList";
    }

    @GetMapping("/list")
    public String commentList() {
        return "post/detail";
    }
}
