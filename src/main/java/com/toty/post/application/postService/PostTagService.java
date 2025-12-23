package com.toty.post.application.postService;

import com.toty.post.domain.model.post.Post;
import com.toty.post.domain.model.post.PostTag;
import com.toty.post.domain.repository.post.PostTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostTagService {
    public final PostTagRepository postTagRepository;

    public List<PostTag> getTagsByPost(Post post) {
        return postTagRepository.findByPost(post);
    }
}
