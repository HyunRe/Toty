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

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    User save(User user);

    @Query("SELECT new com.toty.roleRefreshScheduler.dto.UserIdAndRoleDto(u.id, u.role) FROM User u WHERE u.isDeleted = false")
    List<UserIdAndRoleDto> findAllByIsDeletedFalse();
}