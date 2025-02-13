package com.toty.user.domain.model;

import com.toty.post.domain.model.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_scrapes")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserScrape {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public UserScrape(User user, Post post) {
        this.user = user;
        this.post = post;
        this.createdAt = LocalDateTime.now();
    }
}
