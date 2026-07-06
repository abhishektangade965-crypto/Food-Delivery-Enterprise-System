package com.fooddelivery.order.application.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record TrackOrderResponse(
    UUID orderTrackingId,
    String orderStatus,
    List<String> failureMessages
) {}
