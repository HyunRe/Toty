package com.toty.following.domain;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface FollowingRepository extends Repository<Following, Long> {

    @Query("select count(f) from Following f where f.toUser.id = :id")
    Long countFollowersByUserId(@Param("id") Long id); // 나를 팔로우하는 사람 수

    @Query("select count(f) from Following f where f.fromUser.id = :id")
    Long countFollowingsByUserId(@Param("id") Long id); // 내가 팔로잉하는 사람 수

    boolean existsByFromUserIdAndToUserId(Long fromId, Long toId);

    void save(Following following);

    // 확인 필요
    Following findByFromUserIdAndToUserId(Long fromId, Long toId);

    void deleteById(Long id);

    Page<Following> findPagedFollowingByToUserId(Pageable pageable, Long uid);

    List<Following> findByToUserId(Long uid);

    Page<Following> findPagedFollowingByFromUserId(Pageable pageable, Long uid);
}
