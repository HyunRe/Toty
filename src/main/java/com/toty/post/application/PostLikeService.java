package com.toty.post.application;

import com.toty.base.exception.PostNotFoundException;
import com.toty.base.exception.UserNotFoundException;
import com.toty.post.domain.model.Post;
import com.toty.post.domain.model.PostLike;
import com.toty.post.domain.repository.PostLikeRepository;
import com.toty.post.domain.repository.PostRepository;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;


    // 좋아요 토글 (증감소)
    @Transactional
    public Boolean toggleLikeAction(Long postId, Long userId, String likeAction) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        boolean isLiked = false;

        if ("like".equals(likeAction)) { // 좋아요 추가
            PostLike existingLike = postLikeRepository.findByUserAndPost(user, post).orElse(null);
            if (existingLike == null) {
                PostLike newLike = new PostLike(user, post);
                postLikeRepository.save(newLike);
                isLiked = true;
            }
        }
        if ("unlike".equals(likeAction)) { // 좋아요 취소
            postLikeRepository.findByUserAndPost(user, post).ifPresent(postLikeRepository::delete);
        }

        // 좋아요 개수 갱신
        int likeCount = postLikeRepository.countPostLikesByPost(post);
        post.updateLikeCount(likeCount);

        return isLiked;
    }

    // 게시물의 좋아요 개수 가져오기
    @Transactional(readOnly = true)
    public int getLikeCount(Long id) {
        Post post = postRepository.findById(id).orElseThrow(PostNotFoundException::new);
        return postLikeRepository.countPostLikesByPost(post);
    }
}
