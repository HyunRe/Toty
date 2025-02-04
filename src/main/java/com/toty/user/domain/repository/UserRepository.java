package com.toty.user.domain.repository;

import com.toty.user.domain.model.User;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {

    Optional<User> findByEmail(String email);

    User save(User user);

    Optional<User> findById(Long id);


}