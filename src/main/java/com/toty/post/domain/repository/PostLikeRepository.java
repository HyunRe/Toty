package com.toty.post.domain.repository;

import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostLike;
import com.toty.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    // 좋아요 찾기
    Optional<PostLike> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);

    // 게시글에 대한 좋아요 개수만 조회
    @Query("SELECT COUNT(l) FROM PostLike l WHERE l.post = :post")
    int countPostLikesByPost(@Param("post") Post post);
}
