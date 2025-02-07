package com.toty.user.application;

import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.pagination.PostPaginationStrategy;
import com.toty.post.dto.convert.postlist.PostListResponseContext;
import com.toty.post.dto.response.postlist.PostListResponse;
import com.toty.user.domain.repository.UserScrapeRepository;
import java.util.List;
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

        Pageable pageable = PageRequest.of(page-1, PAGE_SIZE, Sort.by(Sort.Order.desc("updatedAt")));

        Page<Post> posts = userScrapeRepository.findPostsByUserIdAndPostCategory(id, postCategory, pageable);

        PostListResponseContext context = new PostListResponseContext(postCategory);
        List<? extends PostListResponse> postLists = context.convertPosts(posts.getContent());

        return postPaginationStrategy.getPaginationResult(posts, PAGE_SIZE, postLists);
    }

}
