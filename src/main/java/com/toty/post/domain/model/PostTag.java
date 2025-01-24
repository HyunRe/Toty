package com.toty.post.domain.model;

import com.toty.base.domain.model.Tag;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_tags")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "tag_name", nullable = false)
    private Tag tagName;

    public PostTag(Post post, Tag tagName) {
        this.post = post;
        this.tagName = tagName;
    }
}

