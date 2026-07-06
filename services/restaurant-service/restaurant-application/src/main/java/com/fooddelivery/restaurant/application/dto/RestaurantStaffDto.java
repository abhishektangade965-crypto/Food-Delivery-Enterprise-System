package com.fooddelivery.restaurant.application.dto;

import java.time.LocalTime;
import java.util.UUID;

public record RestaurantStaffDto(
    UUID userId,
    String role,
    LocalTime shiftStart,
    LocalTime shiftEnd,
    boolean isActive
) {}
