package com.toty.user.domain;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends Repository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    User save(User user);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deleted = false")
    Optional<User> findById(Long id);

    @Transactional
    @Modifying
    @Query("update User u set u.deleted = true, u.deletedAt = CURRENT_TIMESTAMP where u.id = :id")
    void softDeleteById(@Param("id") Long id);

    boolean existsByPhoneNumber(String phoneNumber);

}