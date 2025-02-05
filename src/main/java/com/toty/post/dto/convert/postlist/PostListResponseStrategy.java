package com.toty.post.dto.convert.postlist;

import com.toty.post.domain.model.Post;
import com.toty.post.dto.response.postlist.PostListResponse;

public interface PostListResponseStrategy {
    PostListResponse convert(Post post);
}
