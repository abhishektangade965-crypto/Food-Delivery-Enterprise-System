package com.fooddelivery.user.application.dto;

import java.util.Set;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    String userId,
    String email,
    Set<String> roles
) {}
