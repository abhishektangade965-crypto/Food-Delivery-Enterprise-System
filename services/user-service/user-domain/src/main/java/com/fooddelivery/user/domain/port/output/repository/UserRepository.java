package com.fooddelivery.user.domain.port.output.repository;

import com.fooddelivery.user.domain.entity.User;
import com.fooddelivery.user.domain.valueobject.UserId;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId userId);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
}
