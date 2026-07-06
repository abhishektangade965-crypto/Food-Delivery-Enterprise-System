package com.fooddelivery.user.application.dto;

import java.time.LocalDate;
import java.util.Set;

public record UserResponse(
    String userId,
    String email,
    String firstName,
    String lastName,
    String phone,
    LocalDate dateOfBirth,
    String status,
    Set<String> roles,
    boolean mfaEnabled
) {}
