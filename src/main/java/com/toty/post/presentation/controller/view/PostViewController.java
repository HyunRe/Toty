package com.toty.post.presentation.controller.view;

import com.toty.post.application.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/posts")
@RequiredArgsConstructor
public class PostViewController {
    private final PostService postService;

}
