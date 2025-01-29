package com.toty.user.domain;

import java.util.List;
import org.springframework.data.repository.Repository;

public interface UserTagRepository extends Repository<UserTag, Long> {
    List<UserTag> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    UserTag save(UserTag userTag);
}
