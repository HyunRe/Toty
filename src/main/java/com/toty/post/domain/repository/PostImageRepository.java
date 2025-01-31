package com.toty.post.domain.repository;

import com.toty.post.domain.model.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.*;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
}
