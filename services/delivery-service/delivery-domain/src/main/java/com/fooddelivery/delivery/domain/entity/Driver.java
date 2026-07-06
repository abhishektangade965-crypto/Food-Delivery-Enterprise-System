package com.fooddelivery.delivery.domain.entity;

import com.fooddelivery.common.domain.entity.AggregateRoot;
import com.fooddelivery.common.domain.valueobject.GeoLocation;
import com.fooddelivery.delivery.domain.valueobject.DriverId;
import com.fooddelivery.delivery.domain.valueobject.DriverStatus;
import com.fooddelivery.delivery.domain.valueobject.DriverApprovalStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class Driver extends AggregateRoot<DriverId> {
    private final UUID userId;
    private String vehicleType;
    private String vehicleNumber;
    private String licenseNumber;
    private DriverStatus status;
    private DriverApprovalStatus approvalStatus;
    private Double rating;
    private Integer totalDeliveries;
    private Double acceptanceRate;
    private BigDecimal walletBalance;
    private Boolean faceVerified;
    private Boolean isActive;
    private GeoLocation location;

    @Builder
    public Driver(DriverId driverId, UUID userId, String vehicleType, String vehicleNumber, 
                  String licenseNumber, DriverStatus status, DriverApprovalStatus approvalStatus, 
                  Double rating, Integer totalDeliveries, Double acceptanceRate, 
                  BigDecimal walletBalance, Boolean faceVerified, Boolean isActive, GeoLocation location) {
        super(driverId);
        this.userId = userId;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.licenseNumber = licenseNumber;
        this.status = status;
        this.approvalStatus = approvalStatus;
        this.rating = rating;
        this.totalDeliveries = totalDeliveries;
        this.acceptanceRate = acceptanceRate;
        this.walletBalance = walletBalance;
        this.faceVerified = faceVerified;
        this.isActive = isActive;
        this.location = location;
    }

    public void initializeDriver() {
        setId(DriverId.generate());
        this.status = DriverStatus.OFFLINE;
        this.approvalStatus = DriverApprovalStatus.PENDING;
        this.rating = 5.0;
        this.totalDeliveries = 0;
        this.acceptanceRate = 1.0;
        this.walletBalance = BigDecimal.ZERO;
        this.faceVerified = false;
        this.isActive = true;
    }
}
