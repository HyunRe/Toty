package com.toty.user.application;

import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostCategory;
import com.toty.post.domain.pagination.PostPaginationStrategy;
import com.toty.post.dto.convert.postlist.PostListResponseContext;
import com.toty.post.dto.response.postlist.PostListResponse;
import com.toty.user.domain.repository.UserScrapeRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserScrapeService {
    private final UserScrapeRepository userScrapeRepository;
    private final PostPaginationStrategy postPaginationStrategy;
    private static final int PAGE_SIZE = 10;

    @Transactional(readOnly = true)
    public PaginationResult getPagedPostsByMyScrape(Long id, int page, String postCategory) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by(Sort.Order.desc("updatedAt")));
        // postCategory null이면 free(일반 게시판)로 처리
        Map<String, PostCategory> categoryMap = Map.of(
                "General", PostCategory.GENERAL,
                "Qna", PostCategory.QnA,
                "knowledge", PostCategory.KNOWLEDGE
        );

        PostCategory pc = categoryMap.getOrDefault(
                Optional.ofNullable(postCategory).map(String::toLowerCase).orElse("free"),
                PostCategory.GENERAL
        );

        Page<Post> posts = userScrapeRepository.findPostsByUserIdAndPostCategory(id, pc, pageable);

        List<? extends PostListResponse> postLists = new PostListResponseContext(postCategory)
                .convertPosts(posts.getContent());

        return postPaginationStrategy.getPaginationResult(posts, PAGE_SIZE, postLists);
    }

}
