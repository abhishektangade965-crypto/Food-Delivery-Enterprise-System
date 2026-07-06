package com.fooddelivery.delivery.application.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentResponseDto {
    private UUID assignmentId;
    private UUID orderId;
    private UUID driverId;
    private UUID batchId;
    private String status;
    private LocationDto pickupLocation;
    private LocationDto dropoffLocation;
    private ZonedDateTime estimatedPickupTime;
    private ZonedDateTime estimatedDeliveryTime;
    private ZonedDateTime actualPickupTime;
    private ZonedDateTime actualDeliveryTime;
    private Double distanceKm;
    private String otp;
    private Boolean otpVerified;
    private String proofOfDeliveryUrl;
    private String driverNotes;
    private BigDecimal deliveryFee;
    private BigDecimal tipAmount;
    private ZonedDateTime createdAt;
}
