package com.toty.post.application;

import com.toty.comment.application.CommentPaginationService;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.pagination.PostPaginationStrategy;
import com.toty.post.domain.repository.PostRepository;
import com.toty.post.domain.specification.PostSpecifications;
import com.toty.post.dto.convert.postdetail.PostDetailResponseContext;
import com.toty.post.dto.convert.postlist.PostListResponseContext;
import com.toty.post.dto.response.postdetail.PostDetailResponse;
import com.toty.post.dto.response.postlist.GeneralPostListResponse;
import com.toty.post.dto.response.postlist.PostListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostPaginationService {
    private static final int PAGE_SIZE = 10;  // 기본 페이지 수

    private final PostRepository postRepository;
    private final PostPaginationStrategy postPaginationStrategy;
    private final CommentPaginationService commentPaginationService;

    // 게시글 가져 오기
    public Post findPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.POST_NOT_FOUND));
    }

    // 전체 게시글 목록 조회 (수정시간 기준으로 최신순 정렬 - 오늘 / 이번 주 / 이번 딜)
    @Transactional(readOnly = true)
    public PaginationResult getPagedPosts(int page, String filter) {
        Specification<Post> specification = PostSpecifications.isNotDeleted();
        if ("today".equals(filter)) {
            specification = specification.and(PostSpecifications.isToday());
        } else if ("thisWeek".equals(filter)) {
            specification = specification.and(PostSpecifications.isThisWeek());
        } else if ("thisMonth".equals(filter)) {
            specification = specification.and(PostSpecifications.isThisMonth());
        }

        PageRequest pageRequest = PageRequest.of(page - 1, PAGE_SIZE, Sort.by(Sort.Order.desc("updatedAt")));
        Page<Post> posts = postRepository.findAll(specification, pageRequest);

        // Post -> PostListResponse 변환
        List<GeneralPostListResponse> postLists = posts.getContent().stream()
                .map(this::toGeneralPostListResponse)
                .toList();

        return postPaginationStrategy.getPaginationResult(posts, PAGE_SIZE, postLists);
    }

    // 사용자 ID로 필터링된 게시글 목록 조회
    @Transactional(readOnly = true)
    public PaginationResult getPagedPostsByUserId(int page, Long userId, String postCategory) {
        PageRequest pageRequest = PageRequest.of(page - 1, PAGE_SIZE, Sort.by(Sort.Order.asc("updatedAt")));
        Page<Post> posts = postRepository.findAll(
                Specification.where(PostSpecifications.isNotDeleted())
                        .and(PostSpecifications.hasUserId(userId))
                        .and(PostSpecifications.hasCategory(postCategory)),
                pageRequest);

        PostListResponseContext context = new PostListResponseContext(postCategory);
        List<? extends PostListResponse> postLists = context.convertPosts(posts.getContent());

        return postPaginationStrategy.getPaginationResult(posts, PAGE_SIZE, postLists);
    }

    // 카테고리로 필터링된 게시글 목록 조회
    @Transactional(readOnly = true)
    public PaginationResult getPagedPostsByCategory(int page, String postCategory) {
        PageRequest pageRequest = PageRequest.of(page - 1, PAGE_SIZE, Sort.by(Sort.Order.asc("updatedAt")));
        Page<Post> posts = postRepository.findAll(
                Specification.where(PostSpecifications.isNotDeleted())
                        .and(PostSpecifications.hasCategory(postCategory)),
                pageRequest);

        PostListResponseContext context = new PostListResponseContext(postCategory);
        List<? extends PostListResponse> postLists = context.convertPosts(posts.getContent());

        return postPaginationStrategy.getPaginationResult(posts, PAGE_SIZE, postLists);
    }

    // 카테고리 별 게시글 상세 보기
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetailByCategory(int page, Long id, String postCategory) {
        Post post = findPostById(id);
        // 댓글 목록 조회
        PaginationResult pagedComments = commentPaginationService.getPagedCommentsByPostId(page, id);

        PostDetailResponseContext context = new PostDetailResponseContext(postCategory);
        return context.convertPost(post, pagedComments);
    }

    // Post -> PostListResponse
    private GeneralPostListResponse toGeneralPostListResponse(Post post) {
        return new GeneralPostListResponse(
                post.getUser().getNickname(),                   // 사용자 닉네임
                post.getUser().getProfileImageUrl(),            // 프로필 이미지 URL
                post.getTitle(),                                // 제목
                post.getViewCount(),                            // 조회수
                post.getLikeCount(),                     // 좋아요 수
                post.getUpdatedAt()                             // 생성 일시과 수정 일시 중 더 나중에 된 시간
        );
    }
}
