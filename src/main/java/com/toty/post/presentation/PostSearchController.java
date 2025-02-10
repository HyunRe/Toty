package com.toty.post.presentation;

import com.toty.common.response.TotyResponse;
import com.toty.post.application.PostSearchService;
import com.toty.post.domain.model.PostCategory;
import com.toty.post.domain.model.elasticsearch.PostEs;
import com.toty.post.domain.model.elasticsearch.SearchField;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search/posts")
public class PostSearchController {

    private final PostSearchService postSearchService;

    /**
     * 통합(일반 게시글, 정보 게시글, QnA 게시글) 검색 API
     *
     * @param keyword 검색어
     * @param field 검색 속성 (제목, 본문, 제목 + 본문)
     * @param page 페이지 번호 (기본값: 1)
     * @param size 페이지당 게시글 개수 (기본값: 10)
     * @return Map<게시판 종류(일반, 정보, QnA), Page<게시글(본문, 댓글 제외)>>
     */
    @GetMapping
    public TotyResponse<Map<PostCategory, Page<PostEs>>> searchPosts (
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "field", defaultValue = "TITLE") SearchField field,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return TotyResponse.success(postSearchService.searchPosts(keyword, field, page, size));
    }

    @PostMapping("/create")
    public TotyResponse<String> createPost(
            @RequestParam(name = "title") String title,
            @RequestParam(name = "content") String content,
            @RequestParam(name = "category") PostCategory category) {
        String nickname = "테스트닉네임";
        String postId = postSearchService.savePost(title, content, nickname, category);
        return TotyResponse.success(postId);
    }
}

