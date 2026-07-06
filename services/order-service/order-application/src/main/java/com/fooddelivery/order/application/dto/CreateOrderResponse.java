package com.fooddelivery.order.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateOrderResponse(
    UUID orderTrackingId,
    String orderStatus,
    String message
) {}
