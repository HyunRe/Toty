package com.toty.post.domain.repository.post;

import com.toty.common.pagination.PaginationRepository;
import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostCategory;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends PaginationRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    // 조회수 증가 (동시성 고려)
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void updateViewCount(@Param("id") Long id);

    /**
     * N+1 문제 해결: User 정보를 fetch join으로 함께 조회
     * 게시글 목록 조회 시 사용 (삭제된 사용자 제외)
     */
    @Query(value = "SELECT DISTINCT p FROM Post p " +
                   "LEFT JOIN FETCH p.user u " +
                   "WHERE u.isDeleted = false " +
                   "ORDER BY p.updatedAt DESC",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.user.isDeleted = false")
    Page<Post> findAllWithUser(Pageable pageable);

    /**
     * 카테고리별 게시글 조회 (fetch join, 삭제된 사용자 제외)
     */
    @Query(value = "SELECT DISTINCT p FROM Post p " +
                   "LEFT JOIN FETCH p.user u " +
                   "WHERE u.isDeleted = false AND p.postCategory = :category " +
                   "ORDER BY p.updatedAt DESC",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.user.isDeleted = false AND p.postCategory = :category")
    Page<Post> findByCategoryWithUser(@Param("category") PostCategory category, Pageable pageable);

    /**
     * 사용자별 게시글 조회 (fetch join, 삭제된 사용자 제외)
     */
    @Query(value = "SELECT DISTINCT p FROM Post p " +
                   "LEFT JOIN FETCH p.user u " +
                   "WHERE u.isDeleted = false AND p.user.id = :userId " +
                   "ORDER BY p.updatedAt DESC",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.user.isDeleted = false AND p.user.id = :userId")
    Page<Post> findByUserIdWithUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * 사용자 + 카테고리별 게시글 조회 (fetch join, 삭제된 사용자 제외)
     */
    @Query(value = "SELECT DISTINCT p FROM Post p " +
                   "LEFT JOIN FETCH p.user u " +
                   "WHERE u.isDeleted = false AND p.user.id = :userId AND p.postCategory = :category " +
                   "ORDER BY p.updatedAt DESC",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.user.isDeleted = false AND p.user.id = :userId AND p.postCategory = :category")
    Page<Post> findByUserIdAndCategoryWithUser(@Param("userId") Long userId,
                                                @Param("category") PostCategory category,
                                                Pageable pageable);
}
