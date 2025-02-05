package com.toty.comment.presentation.comtroller.view;

import com.toty.comment.application.CommentService;
import com.toty.comment.domain.model.Comment;
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
    public String createCommentForm() {
        return "post/detail";
    }

    @PostMapping("/create")
    public String createComment(@RequestParam("userId") Long userId,
                                @RequestParam("postId") Long postId,
                                @ModelAttribute @Valid CommentCreateUpdateRequest commentCreateUpdateRequest,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "post/detail"; // 유효성 검사 실패 시, 다시 폼을 반환
        }
        commentService.createComment(userId, postId, commentCreateUpdateRequest);
        return "post/detail";
    }

    @GetMapping("/update/{id}")
    public String updateCommentForm(@PathVariable Long id, Model model) {
        Comment comment = commentService.findByCommentId(id);
        model.addAttribute("comment", comment);
        return "post/detail";
    }

    @PatchMapping("/{id}")
    public String updateComment(@PathVariable Long id,
                                @ModelAttribute @Valid CommentCreateUpdateRequest commentCreateUpdateRequest,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "post/detail"; // 유효성 검사 실패 시, 다시 폼을 반환
        }
        commentService.updateComment(id, commentCreateUpdateRequest);
        return "post/detail";
    }

    @GetMapping("/list")
    public String commentList() {
        return "post/detail";
    }
}
