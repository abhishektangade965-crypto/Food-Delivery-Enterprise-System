package com.fooddelivery.order.application.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDto(
    @NotNull(message = "Product ID is required")
    UUID productId,

    @NotNull(message = "Quantity is required")
    int quantity,

    @NotNull(message = "Price is required")
    BigDecimal price,

    @NotNull(message = "Subtotal is required")
    BigDecimal subTotal
) {}
