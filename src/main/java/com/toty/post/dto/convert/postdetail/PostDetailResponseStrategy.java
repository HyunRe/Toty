package com.toty.post.dto.convert.postdetail;

import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.post.Post;
import com.toty.post.dto.response.postdetail.PostDetailResponse;

public interface PostDetailResponseStrategy {
    /**
     * Post 엔티티와 댓글 페이징 결과, 좋아요/스크랩 상태를 기반으로 DTO 생성
     *
     * @param post          게시글 엔티티
     * @param pagedComments 댓글 페이징 결과
     * @param isLiked       현재 로그인 사용자의 좋아요 여부
     * @param isScraped     현재 로그인 사용자의 스크랩 여부
     * @return PostDetailResponse DTO
     */
    PostDetailResponse convert(Post post, PaginationResult pagedComments, boolean isLiked, boolean isScraped);
}
