package com.fooddelivery.restaurant.application.dto;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record MenuCategoryDto(
    UUID id,
    String name,
    String description,
    Integer displayOrder,
    boolean isActive,
    LocalTime availableFrom,
    LocalTime availableTo,
    String imageUrl,
    List<MenuItemDto> items
) {}
