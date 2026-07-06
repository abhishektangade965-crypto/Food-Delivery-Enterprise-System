package com.fooddelivery.delivery.domain.entity;

import com.fooddelivery.common.domain.entity.AggregateRoot;
import com.fooddelivery.common.domain.valueobject.GeoLocation;
import com.fooddelivery.common.domain.valueobject.OrderId;
import com.fooddelivery.delivery.domain.valueobject.AssignmentId;
import com.fooddelivery.delivery.domain.valueobject.AssignmentStatus;
import com.fooddelivery.delivery.domain.valueobject.DriverId;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
public class DeliveryAssignment extends AggregateRoot<AssignmentId> {
    private final OrderId orderId;
    private DriverId driverId;
    private UUID batchId;
    private AssignmentStatus status;
    private GeoLocation pickupLocation;
    private GeoLocation dropoffLocation;
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
    private final ZonedDateTime createdAt;

    @Builder
    public DeliveryAssignment(AssignmentId assignmentId, OrderId orderId, DriverId driverId, 
                              UUID batchId, AssignmentStatus status, GeoLocation pickupLocation, 
                              GeoLocation dropoffLocation, ZonedDateTime estimatedPickupTime, 
                              ZonedDateTime estimatedDeliveryTime, ZonedDateTime actualPickupTime, 
                              ZonedDateTime actualDeliveryTime, Double distanceKm, String otp, 
                              Boolean otpVerified, String proofOfDeliveryUrl, String driverNotes, 
                              BigDecimal deliveryFee, BigDecimal tipAmount, ZonedDateTime createdAt) {
        super(assignmentId);
        this.orderId = orderId;
        this.driverId = driverId;
        this.batchId = batchId;
        this.status = status;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.estimatedPickupTime = estimatedPickupTime;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.actualPickupTime = actualPickupTime;
        this.actualDeliveryTime = actualDeliveryTime;
        this.distanceKm = distanceKm;
        this.otp = otp;
        this.otpVerified = otpVerified;
        this.proofOfDeliveryUrl = proofOfDeliveryUrl;
        this.driverNotes = driverNotes;
        this.deliveryFee = deliveryFee;
        this.tipAmount = tipAmount;
        this.createdAt = createdAt;
    }

    public void initializeAssignment() {
        setId(AssignmentId.generate());
        this.status = AssignmentStatus.SEARCHING;
        this.otpVerified = false;
        this.otp = String.format("%06d", (int) (Math.random() * 1000000));
        this.deliveryFee = BigDecimal.ZERO;
        this.tipAmount = BigDecimal.ZERO;
    }
}
