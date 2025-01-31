package com.toty.post.domain.repository;

import com.toty.post.domain.model.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
}
