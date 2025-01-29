package com.toty.user.domain;

import java.util.List;
import org.springframework.data.repository.Repository;

public interface UserLinkRepository extends Repository<UserLink, Long> {
    List<UserLink> findByUserId(Long userId);

    void deleteAllByUserId(Long userId);

    UserLink save(UserLink userTag);

}
