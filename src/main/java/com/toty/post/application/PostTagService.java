package com.toty.post.application;

import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostTag;
import com.toty.post.domain.repository.PostTagRepository;
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
