package com.toty.user.domain;

import com.toty.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "tag_name")
    @Enumerated(EnumType.STRING)
    private Tag tag;

    public UserTag(User user, Tag tag) {
        this.user = user;
        this.tag = tag;
    }
}
