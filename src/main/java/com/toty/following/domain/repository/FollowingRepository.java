package com.toty.following.domain.repository;

import java.util.List;

import com.toty.following.domain.model.Following;
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

    Following findByFromUserIdAndToUserId(Long fromId, Long toId);

    void deleteById(Long id);

    // N+1 문제 해결을 위한 fetch join 쿼리
    @Query(value = "SELECT DISTINCT f FROM Following f " +
                   "LEFT JOIN FETCH f.fromUser " +
                   "LEFT JOIN FETCH f.toUser " +
                   "WHERE f.toUser.id = :uid",
           countQuery = "SELECT COUNT(f) FROM Following f WHERE f.toUser.id = :uid")
    Page<Following> findPagedFollowingByToUserIdWithUsers(Pageable pageable, @Param("uid") Long uid);

    @Query("SELECT DISTINCT f FROM Following f " +
           "LEFT JOIN FETCH f.fromUser " +
           "LEFT JOIN FETCH f.toUser " +
           "WHERE f.toUser.id = :uid")
    List<Following> findByToUserIdWithUsers(@Param("uid") Long uid);

    @Query(value = "SELECT DISTINCT f FROM Following f " +
                   "LEFT JOIN FETCH f.fromUser " +
                   "LEFT JOIN FETCH f.toUser " +
                   "WHERE f.fromUser.id = :uid",
           countQuery = "SELECT COUNT(f) FROM Following f WHERE f.fromUser.id = :uid")
    Page<Following> findPagedFollowingByFromUserIdWithUsers(Pageable pageable, @Param("uid") Long uid);

    // 기존 메서드 (하위 호환성 유지)
    Page<Following> findPagedFollowingByToUserId(Pageable pageable, Long uid);

    List<Following> findByToUserId(Long uid);

    Page<Following> findPagedFollowingByFromUserId(Pageable pageable, Long uid);
}
