package com.fooddelivery.user.application.service;

import com.fooddelivery.user.application.dto.AuthResponse;
import com.fooddelivery.user.application.dto.LoginRequest;
import com.fooddelivery.user.application.dto.RegisterUserRequest;
import com.fooddelivery.user.application.dto.UserResponse;

import java.util.UUID;

public interface UserApplicationService {
    UserResponse registerUser(RegisterUserRequest registerUserRequest);
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse refresh(String refreshToken);
    void logout(String refreshToken);
    UserResponse getUserById(UUID userId);
}
