package com.fooddelivery.user.dataaccess.adapter;

import com.fooddelivery.user.dataaccess.entity.UserJpaEntity;
import com.fooddelivery.user.dataaccess.mapper.UserDataAccessMapper;
import com.fooddelivery.user.dataaccess.repository.UserJpaRepository;
import com.fooddelivery.user.domain.entity.User;
import com.fooddelivery.user.domain.port.output.repository.UserRepository;
import com.fooddelivery.user.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserDataAccessMapper userDataAccessMapper;

    @Override
    public User save(User user) {
        UserJpaEntity jpaEntity = userDataAccessMapper.userToUserJpaEntity(user);
        UserJpaEntity savedEntity = userJpaRepository.save(jpaEntity);
        return userDataAccessMapper.userJpaEntityToUser(savedEntity);
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return userJpaRepository.findById(userId.getValue())
                .map(userDataAccessMapper::userJpaEntityToUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userDataAccessMapper::userJpaEntityToUser);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return userJpaRepository.findByPhone(phone)
                .map(userDataAccessMapper::userJpaEntityToUser);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
}
