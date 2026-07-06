package com.fooddelivery.delivery.application.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverResponseDto {
    private UUID driverId;
    private UUID userId;
    private String vehicleType;
    private String vehicleNumber;
    private String licenseNumber;
    private String status;
    private String approvalStatus;
    private Double rating;
    private Integer totalDeliveries;
    private Double acceptanceRate;
    private BigDecimal walletBalance;
    private Boolean faceVerified;
    private Boolean isActive;
    private LocationDto location;
}
