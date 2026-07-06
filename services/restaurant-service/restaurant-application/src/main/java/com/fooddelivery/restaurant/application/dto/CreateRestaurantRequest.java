package com.fooddelivery.restaurant.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateRestaurantRequest(
    @NotNull UUID ownerId,
    @NotBlank String name,
    @NotBlank String description,
    List<String> cuisineTypes,
    @NotNull BigDecimal minOrderAmount,
    @NotNull BigDecimal deliveryFee,
    @NotNull Integer avgDeliveryTimeMinutes,
    @NotNull Double latitude,
    @NotNull Double longitude,
    @NotBlank String street,
    @NotBlank String city,
    String state,
    @NotBlank String country,
    @NotBlank String postalCode,
    String bankDetails,
    BigDecimal commissionRate
) {}
