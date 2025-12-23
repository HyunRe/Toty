package com.toty.post.dto.convert.postdetail;

import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.post.Post;
import com.toty.post.dto.response.postdetail.PostDetailResponse;

public class PostDetailResponseContext {
    private final PostDetailResponseStrategy strategy;

    public PostDetailResponseContext(String postCategory) {
        if ("general".equals(postCategory)) {
            strategy = new GeneralPostDetailResponseStrategy();
        } else if ("knowledge".equals(postCategory)) {
            strategy = new KnowledgePostDetailResponseStrategy();
        } else if ("qna".equals(postCategory)) {
            strategy = new QnaPostDetailResponseStrategy();
        } else {
            strategy = new GeneralPostDetailResponseStrategy(); // 기본값으로 일반 게시글 전략
        }
    }

    /**
     * 게시글과 댓글, 좋아요/스크랩 상태를 기반으로 DTO 변환
     *
     * @param post           게시글 엔티티
     * @param pagedComments  댓글 페이징 결과
     * @param isLiked        현재 사용자의 좋아요 여부
     * @param isScraped      현재 사용자의 스크랩 여부
     * @return PostDetailResponse DTO
     */
    public PostDetailResponse convertPost(Post post, PaginationResult pagedComments, boolean isLiked, boolean isScraped) {
        return strategy.convert(post, pagedComments, isLiked, isScraped);
    }
}

