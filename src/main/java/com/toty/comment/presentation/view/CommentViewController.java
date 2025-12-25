package com.toty.comment.presentation.view;

import com.toty.comment.application.service.CommentPaginationService;
import com.toty.comment.application.service.CommentService;
import com.toty.comment.domain.model.comment.Comment;
import com.toty.common.annotation.CurrentUser;
import com.toty.common.pagination.PaginationResult;
import com.toty.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/view/comments")
@RequiredArgsConstructor
public class CommentViewController {
    private final CommentService commentService;
    private final CommentPaginationService commentPaginationService;

    @GetMapping("/create")
    public String createComment() {
        return "post/detail";
    }

    @GetMapping("/update/{id}")
    public String updateComment(@PathVariable Long id, Model model) {
        Comment comment = commentService.findByCommentId(id);
        model.addAttribute("comment", comment);
        return "post/detail";
    }

    @GetMapping("/list")
    public String commentList(@RequestParam(name = "page", defaultValue = "1") int page,
                              @RequestParam("postId") Long postId, Model model) {
        PaginationResult result = commentPaginationService.getPagedCommentsByPostId(page, postId);
        model.addAttribute("result", result);
        model.addAttribute("page", page);
        return "post/detail";
    }

    // 내가 작성한 댓글 목록 조회
    @GetMapping("/myList")
    public String myCommentList(@CurrentUser User user,
                                @RequestParam(name = "page", defaultValue = "1") int page,
                                Model model) {
        PaginationResult comments = commentPaginationService.getPagedCommentsByUserId(page, user.getId());
        model.addAttribute("comments", comments);
        model.addAttribute("page", page);
        return "user/myCommentList";
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
        Comment comment = commentService.findByCommentId(id);
        model.addAttribute("comment", comment);
        return "common/dialogMsg";
    }
}