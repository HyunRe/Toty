package com.toty.post.dto.convert.postdetail;

import com.toty.common.pagination.PaginationResult;
import com.toty.post.domain.model.Post;
import com.toty.post.dto.response.postdetail.PostDetailResponse;

public interface PostDetailResponseStrategy {
    PostDetailResponse convert(Post post, PaginationResult pagedComments);
}
