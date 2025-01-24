package com.toty.post.application.strategy.convert.postdetail;

import com.toty.base.pagination.PaginationResult;
import com.toty.post.domain.model.Post;
import com.toty.post.presentation.dto.response.postdetail.PostDetailResponse;

public interface PostDetailResponseStrategy {
    PostDetailResponse convert(Post post, PaginationResult pagedComments);
}
