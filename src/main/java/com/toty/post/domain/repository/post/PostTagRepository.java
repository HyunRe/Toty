package com.toty.post.domain.repository.post;

import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    List<PostTag> findByPost(Post post);
}
