package com.toty.user.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(nullable = false)
    private String nickname; // 필수값

    @Column
    private String profileImageUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserScrape> userScrapes = new ArrayList<>();

    public void addUserScrape(UserScrape userScrape) {
        if (this.userScrapes == null)
            this.userScrapes = new ArrayList<>();
        this.userScrapes.add(userScrape);
        userScrape.setUser(this);
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
