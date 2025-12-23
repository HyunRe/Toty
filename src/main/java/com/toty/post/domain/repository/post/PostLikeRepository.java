package com.toty.post.domain.repository.post;

import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostLike;
import com.toty.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    // 좋아요 찾기
    Optional<PostLike> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);

    // 게시글에 대한 좋아요 개수만 조회
    @Query("SELECT COUNT(l) FROM PostLike l WHERE l.post = :post")
    int countPostLikesByPost(@Param("post") Post post);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM PostLike l WHERE l.user.id = :userId AND l.post.id = :postId")
    boolean existsByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    // 사용자가 좋아요한 게시글 목록 조회 (페이징)
    @Query("SELECT l.post FROM PostLike l WHERE l.user.id = :userId AND l.post.user.isDeleted = false ORDER BY l.createdAt DESC")
    Page<Post> findLikedPostsByUserId(@Param("userId") Long userId, Pageable pageable);
}
