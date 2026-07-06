package com.fooddelivery.order.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TrackOrderQuery(
    @NotNull(message = "Tracking ID is required")
    UUID trackingId
) {}
