package com.toty.user.application;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostCategory;
import com.toty.post.domain.pagination.PostPaginationStrategy;
import com.toty.post.domain.repository.post.PostRepository;
import com.toty.post.dto.convert.postlist.PostListResponseContext;
import com.toty.post.dto.response.postlist.PostListResponse;
import com.toty.user.domain.model.User;
import com.toty.user.domain.model.UserScrape;
import com.toty.user.domain.repository.UserRepository;
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
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserScrapeRepository userScrapeRepository;
    private final PostPaginationStrategy postPaginationStrategy;
    private static final int PAGE_SIZE = 10;

    // 저장된 게시글 목록 조회
    @Transactional(readOnly = true)
    public PaginationResult getPagedPostsByMyScrape(Long id, int page, String postCategory) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by(Sort.Order.desc("createdAt")));
        // postCategory null이면 free(일반 게시판)로 처리
        Map<String, PostCategory> categoryMap = Map.of(
                "general", PostCategory.GENERAL,
                "qna", PostCategory.QnA,
                "knowledge", PostCategory.KNOWLEDGE
        );

        PostCategory pc = categoryMap.getOrDefault(
                Optional.ofNullable(postCategory).map(String::toLowerCase).orElse("general"),
                PostCategory.GENERAL
        );

        Page<Post> posts = userScrapeRepository.findPostsByUserIdAndPostCategory(id, pc, pageable);

        List<? extends PostListResponse> postLists = new PostListResponseContext(postCategory)
                .convertPosts(posts.getContent());

        return postPaginationStrategy.getPaginationResult(posts, PAGE_SIZE, postLists);
    }

    // 게시글 저장 토글
    @Transactional
    public String toggleScrape(Long postId, Long userId, String Scrape) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ExpectedException(ErrorCode.POST_NOT_FOUND));
        System.out.println("postI = " + postId);
        System.out.println("userId = " + userId);
        System.out.println("Scrape = " + Scrape);
        if ("scrape".equals(Scrape)) { // 스크랩 추가
            if (!userScrapeRepository.findByUserAndPost(user, post).isPresent()) {
                System.out.println("값 없음");
                userScrapeRepository.save(new UserScrape(user, post));
            }
        }
        if ("cancel".equals(Scrape)) { // 스크랩 취소
            userScrapeRepository.findByUserAndPost(user, post).ifPresent(userScrapeRepository::delete);
        }
        return "true";
    }

    public boolean isPostScrapedByUser(Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.POST_NOT_FOUND));

        return userScrapeRepository.existsByUserAndPost(user, post);
    }
}