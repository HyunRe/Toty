package com.toty.post.domain.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.toty.common.domain.BaseTime;
import com.toty.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;
import com.toty.comment.domain.model.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "postCategory", nullable = false)
    private PostCategory postCategory;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "view_count", columnDefinition = "int default 0")
    private int viewCount;

    @Column(name = "like_count", columnDefinition = "int default 0")
    private int likeCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonManagedReference
    private List<PostTag> postTags = new ArrayList<>();

    private <T> void addRelatedEntity(List<T> list, T entity, BiConsumer<T, Post> setPostMethod) {
        if (list == null) {
            list = new ArrayList<>();
        }

        int index = list.indexOf(entity);
        if (index != -1) {
            list.set(index, entity);
        } else {
            list.add(entity);
        }

        setPostMethod.accept(entity, this);
    }

    public void addComment(Comment comment) {
        addRelatedEntity(this.comments, comment, Comment::setPost);
    }

    public void updateLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public Post(User user, PostCategory postCategory, String title, String content, int viewCount, int likeCount,
                List<Comment> comments, List<PostTag> postTags) {
        this.user = user;
        this.postCategory = postCategory;
        this.title = title;
        this.content = content;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.comments = comments;
        this.postTags = postTags;
    }

    public Post updatePost(String title, String content, List<PostTag> postTags) {
        this.title = title;
        this.content = content;
        this.postTags = postTags;

        return this;
    }
}
