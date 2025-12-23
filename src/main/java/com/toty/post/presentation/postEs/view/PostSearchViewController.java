package com.toty.post.presentation.postEs.view;

import com.toty.post.domain.model.postEs.SearchField;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/search/posts")
public class PostSearchViewController {

    @GetMapping
    public String search(@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                         @RequestParam(value = "field", required = false) SearchField field,
                         Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("field", field);
        return "post/search";
    }
}