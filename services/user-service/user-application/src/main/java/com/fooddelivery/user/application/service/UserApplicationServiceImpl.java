package com.fooddelivery.user.application.service;

import com.fooddelivery.user.application.dto.AuthResponse;
import com.fooddelivery.user.application.dto.LoginRequest;
import com.fooddelivery.user.application.dto.RegisterUserRequest;
import com.fooddelivery.user.application.dto.UserResponse;
import com.fooddelivery.user.application.mapper.UserMapper;
import com.fooddelivery.user.domain.entity.User;
import com.fooddelivery.user.domain.event.UserCreatedEvent;
import com.fooddelivery.user.domain.exception.UserDomainException;
import com.fooddelivery.user.domain.exception.UserNotFoundException;
import com.fooddelivery.user.domain.port.output.repository.UserRepository;
import com.fooddelivery.user.domain.service.UserDomainService;
import com.fooddelivery.user.domain.valueobject.UserId;
import com.fooddelivery.user.domain.valueobject.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationServiceImpl implements UserApplicationService {

    private final UserDomainService userDomainService;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public UserResponse registerUser(RegisterUserRequest registerUserRequest) {
        if (userRepository.existsByEmail(registerUserRequest.email())) {
            throw new UserDomainException("User with email " + registerUserRequest.email() + " already exists");
        }

        User user = userMapper.registerRequestToUser(registerUserRequest);
        UserCreatedEvent userCreatedEvent = userDomainService.createUser(user);

        userCreatedEvent.getUser().setPasswordHash(passwordEncoder.encode(registerUserRequest.password()));
        userCreatedEvent.getUser().setRoles(Set.of(UserRole.CUSTOMER));
        userCreatedEvent.getUser().activate(); // For simplified walkthrough, activate directly

        User savedUser = userRepository.save(userCreatedEvent.getUser());
        log.info("Successfully registered and saved user with id: {}", savedUser.getId().getValue());

        return userMapper.userToUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new UserDomainException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
            throw new UserDomainException("Invalid email or password");
        }

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                900, // 15 minutes expiration
                user.getId().getValue().toString(),
                user.getEmail(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        String userIdStr = tokenService.validateRefreshTokenAndGetUserId(refreshToken);
        if (userIdStr == null) {
            throw new UserDomainException("Invalid or expired refresh token");
        }

        User user = userRepository.findById(new UserId(UUID.fromString(userIdStr)))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String newAccessToken = tokenService.generateAccessToken(user);
        String newRefreshToken = tokenService.generateRefreshToken(user);
        tokenService.invalidateRefreshToken(refreshToken);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                900,
                user.getId().getValue().toString(),
                user.getEmail(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
        );
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        tokenService.invalidateRefreshToken(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return userMapper.userToUserResponse(user);
    }
}
