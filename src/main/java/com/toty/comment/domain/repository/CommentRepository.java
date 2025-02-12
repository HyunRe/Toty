package com.toty.comment.domain.repository;

import com.toty.common.pagination.PaginationRepository;
import com.toty.comment.domain.model.Comment;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommentRepository extends PaginationRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
}
