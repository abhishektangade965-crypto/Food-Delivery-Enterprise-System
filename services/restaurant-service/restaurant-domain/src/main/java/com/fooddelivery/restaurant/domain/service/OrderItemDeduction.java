package com.fooddelivery.restaurant.domain.service;

import com.fooddelivery.restaurant.domain.valueobject.InventoryItemId;
import java.math.BigDecimal;

public record OrderItemDeduction(
    InventoryItemId inventoryItemId,
    BigDecimal quantity
) {}
