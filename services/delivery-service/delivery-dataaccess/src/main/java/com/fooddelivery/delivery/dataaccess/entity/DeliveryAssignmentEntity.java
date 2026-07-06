package com.fooddelivery.delivery.dataaccess.entity;

import com.fooddelivery.delivery.domain.valueobject.AssignmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "delivery_assignments")
@Entity
public class DeliveryAssignmentEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "driver_id")
    private UUID driverId;

    @Column(name = "batch_id")
    private UUID batchId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    @Column(name = "pickup_latitude", nullable = false)
    private Double pickupLatitude;

    @Column(name = "pickup_longitude", nullable = false)
    private Double pickupLongitude;

    @Column(name = "dropoff_latitude", nullable = false)
    private Double dropoffLatitude;

    @Column(name = "dropoff_longitude", nullable = false)
    private Double dropoffLongitude;

    @Column(name = "estimated_pickup_time")
    private ZonedDateTime estimatedPickupTime;

    @Column(name = "estimated_delivery_time")
    private ZonedDateTime estimatedDeliveryTime;

    @Column(name = "actual_pickup_time")
    private ZonedDateTime actualPickupTime;

    @Column(name = "actual_delivery_time")
    private ZonedDateTime actualDeliveryTime;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    private String otp;

    @Column(name = "otp_verified", nullable = false)
    private Boolean otpVerified;

    @Column(name = "proof_of_delivery_url")
    private String proofOfDeliveryUrl;

    @Column(name = "driver_notes")
    private String driverNotes;

    @Column(name = "delivery_fee", nullable = false)
    private BigDecimal deliveryFee;

    @Column(name = "tip_amount", nullable = false)
    private BigDecimal tipAmount;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryAssignmentEntity that = (DeliveryAssignmentEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
