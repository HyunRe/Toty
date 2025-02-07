package com.toty.user.domain.repository;

import com.toty.post.domain.model.Post;
import com.toty.user.domain.model.UserScrape;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface UserScrapeRepository extends Repository<UserScrape, Long> {

    @Query("SELECT p FROM UserScrape us JOIN us.post p WHERE us.user.id = :userId AND p.postCategory = :postCategory")
    Page<Post> findPostsByUserIdAndPostCategory(@Param("userId") Long userId, @Param("postCategory") String postCategory, Pageable pageable);

}
