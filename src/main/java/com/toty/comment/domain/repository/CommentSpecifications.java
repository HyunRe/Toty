package com.toty.comment.domain.repository;

import com.toty.comment.domain.model.Comment;
import org.springframework.data.jpa.domain.Specification;

public class CommentSpecifications {
    // 삭제 되지 않은 사용자 데이터 필터링
    public static Specification<Comment> isNotDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("user").get("isDeleted"));
    }

    // 특정 게시글 ID로 필터링
    public static Specification<Comment> hasPostId(Long postId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("post").get("id"), postId);
    }
}
