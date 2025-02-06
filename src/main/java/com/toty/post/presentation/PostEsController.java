package com.toty.post.presentation;

import com.toty.common.annotation.CurrentUser;
import com.toty.post.application.PostEsService;
import com.toty.post.domain.model.elasticsearch.PostEs;
import com.toty.post.domain.model.elasticsearch.SearchField;
import com.toty.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/search/posts")
public class PostEsController {

    private final PostEsService postEsService;

    // 기본 검색 ( 제목, 내용, 제목+내용 )
    @GetMapping
    @ResponseBody
    public Page<PostEs> searchPosts(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "field", defaultValue = "TITLE") SearchField field,
            @CurrentUser User user
    ) {
        return postEsService.searchPosts(keyword, field, page);
    }
}