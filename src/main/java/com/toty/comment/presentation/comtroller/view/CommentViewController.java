package com.toty.comment.presentation.comtroller.view;

import com.toty.comment.application.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/comments")
@RequiredArgsConstructor
public class CommentViewController {
    private final CommentService commentService;

    @GetMapping("/list")
    public String commentList() {
        return "post/detail";
    }
}
