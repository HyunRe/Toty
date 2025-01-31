package com.toty.user.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "site")
    @Enumerated(EnumType.STRING)
    private Site site;

    @Column(name = "url")
    private String url;

    public UserLink(User user, Site site, String url) {
        this.user = user;
        this.site = site;
        this.url = url;
    }
}
