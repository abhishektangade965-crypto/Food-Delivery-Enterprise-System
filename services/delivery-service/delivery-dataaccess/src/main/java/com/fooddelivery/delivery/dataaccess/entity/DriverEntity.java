package com.fooddelivery.delivery.dataaccess.entity;

import com.fooddelivery.delivery.domain.valueobject.DriverApprovalStatus;
import com.fooddelivery.delivery.domain.valueobject.DriverStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "drivers")
@Entity
public class DriverEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "vehicle_type", nullable = false)
    private String vehicleType;

    @Column(name = "vehicle_number", nullable = false)
    private String vehicleNumber;

    @Column(name = "license_number", nullable = false)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private DriverApprovalStatus approvalStatus;

    private Double rating;

    @Column(name = "total_deliveries", nullable = false)
    private Integer totalDeliveries;

    @Column(name = "acceptance_rate", nullable = false)
    private Double acceptanceRate;

    @Column(name = "wallet_balance", nullable = false)
    private BigDecimal walletBalance;

    @Column(name = "face_verified", nullable = false)
    private Boolean faceVerified;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    private Double latitude;
    private Double longitude;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DriverEntity that = (DriverEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
