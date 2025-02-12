package com.toty.post.domain.repository;

import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    List<PostTag> findByPost(Post post);
}
