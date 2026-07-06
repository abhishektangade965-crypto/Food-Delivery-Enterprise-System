package com.fooddelivery.order.application.dto;

import com.fooddelivery.common.domain.valueobject.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
    @NotNull(message = "Customer ID is required")
    UUID customerId,

    @NotNull(message = "Restaurant ID is required")
    UUID restaurantId,

    @NotNull(message = "Price is required")
    BigDecimal price,

    @NotNull(message = "Order items are required")
    @Size(min = 1, message = "Order must have at least 1 item")
    @Valid
    List<OrderItemDto> items,

    @NotNull(message = "Delivery address is required")
    Address deliveryAddress,

    String promoCode,
    String specialInstructions
) {}
