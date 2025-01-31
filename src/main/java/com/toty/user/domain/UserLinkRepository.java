package com.toty.user.domain;

import java.util.List;
import org.springframework.data.repository.Repository;

public interface UserLinkRepository extends Repository<UserLink, Long> {
    List<UserLink> findAllByUserId(Long uid);

    void deleteAllByUserId(Long uid);

    UserLink save(UserLink userTag);

}
