package com.toty.comment.domain.repository;

import com.toty.common.pagination.BaseRepository;
import com.toty.comment.domain.model.Comment;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommentRepository extends BaseRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
}
