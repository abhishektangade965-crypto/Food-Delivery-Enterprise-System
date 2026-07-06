package com.fooddelivery.restaurant.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemIngredientDto(
    UUID inventoryItemId,
    BigDecimal quantityNeeded,
    String unit
) {}
