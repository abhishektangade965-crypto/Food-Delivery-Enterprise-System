package com.fooddelivery.restaurant.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MenuItemDto(
    UUID id,
    String name,
    String description,
    BigDecimal basePrice,
    BigDecimal discountedPrice,
    String foodType,
    boolean isActive,
    boolean isAvailable,
    boolean isFeatured,
    Integer calorieCount,
    Integer prepTimeMinutes,
    String imageUrl,
    List<String> tags,
    List<String> allergens,
    Integer stockQuantity,
    boolean trackInventory,
    BigDecimal rating,
    List<MenuItemIngredientDto> ingredients
) {}
