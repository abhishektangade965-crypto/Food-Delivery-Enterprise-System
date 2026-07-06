package com.fooddelivery.restaurant.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryItemDto(
    UUID id,
    String name,
    String unit,
    BigDecimal currentStock,
    BigDecimal reorderLevel,
    BigDecimal maxStock,
    BigDecimal costPerUnit,
    String supplierInfo
) {}
