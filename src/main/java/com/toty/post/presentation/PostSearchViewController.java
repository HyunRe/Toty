package com.toty.post.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/search/posts")
public class PostSearchViewController {

    @GetMapping
    public String search() {
        return "post/search";
    }
}