package com.toty.post.application.postService;

import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.pagination.PostPaginationStrategy;
import com.toty.post.domain.repository.post.PostLikeRepository;
import com.toty.post.dto.convert.postlist.PostListResponseContext;
import com.toty.post.dto.response.postlist.PostListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostLikePaginationService {
    private static final int PAGE_SIZE = 10;  // 기본 페이지 수

    private final PostLikeRepository postLikeRepository;
    private final PostPaginationStrategy postPaginationStrategy;

    // 사용자가 좋아요한 게시글 목록 조회
    @Transactional(readOnly = true)
    public PaginationResult getLikedPostsByUserId(int page, Long userId, String postCategory) {
        PageRequest pageRequest = PageRequest.of(page - 1, PAGE_SIZE);
        Page<Post> posts = postLikeRepository.findLikedPostsByUserId(userId, pageRequest);

        PostListResponseContext context = new PostListResponseContext(postCategory);
        List<? extends PostListResponse> postLists = context.convertPosts(posts.getContent());

        return postPaginationStrategy.getPaginationResult(posts, PAGE_SIZE, postLists);
    }
}
