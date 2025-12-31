package com.toty.following.domain.model;

import com.toty.common.domain.BaseTime;
import com.toty.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "following", indexes = {
    @Index(name = "idx_from_id", columnList = "from_id"),
    @Index(name = "idx_to_id", columnList = "to_id"),
    @Index(name = "idx_from_to", columnList = "from_id, to_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Following extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_id")
    private User fromUser; // 이후 user에서 추가 fetch..?

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_id")
    private User toUser;

    public Following(User fromUser, User toUser) {
        this.fromUser = fromUser;
        this.toUser = toUser;
    }
}
