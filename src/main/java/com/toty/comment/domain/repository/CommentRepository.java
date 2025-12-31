package com.toty.comment.domain.repository;

import com.toty.common.pagination.PaginationRepository;
import com.toty.comment.domain.model.comment.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends PaginationRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    /**
     * N+1 문제 해결을 위한 fetch join 쿼리
     * 댓글 목록 조회 시 User 정보도 함께 로드
     */
    @Query("SELECT c FROM Comment c " +
           "LEFT JOIN FETCH c.user " +
           "WHERE c.post.id = :postId " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findByPostIdWithUser(@Param("postId") Long postId);

    /**
     * 페이징 처리가 필요한 경우 (COUNT 쿼리 별도)
     */
    @Query(value = "SELECT c FROM Comment c " +
                   "LEFT JOIN FETCH c.user " +
                   "WHERE c.post.id = :postId",
           countQuery = "SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    Page<Comment> findByPostIdWithUserPaged(@Param("postId") Long postId, Pageable pageable);
}
