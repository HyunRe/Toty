package com.toty.user.domain.repository;

import com.toty.roleRefreshScheduler.dto.UserIdAndRoleDto;
import com.toty.user.domain.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isDeleted = false")
    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    User save(User user);

    List<UserIdAndRoleDto> findAllByIsDeletedFalse();
}