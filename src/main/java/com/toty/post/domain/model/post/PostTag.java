package com.toty.post.domain.model.post;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.toty.common.domain.Tag;
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

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonBackReference
    private Post post;

    @Column(name = "tag_name", nullable = false)
    private Tag tagName;

    public PostTag(Post post, Tag tagName) {
        this.post = post;
        this.tagName = tagName;
    }
}

