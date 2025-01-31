package com.toty.post.domain.repository;

import com.toty.base.domain.repository.BaseRepository;
import com.toty.post.domain.model.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends BaseRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    // 조회수 증가 (동시성 고려)
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void updateViewCount(@Param("id") Long id);
}
