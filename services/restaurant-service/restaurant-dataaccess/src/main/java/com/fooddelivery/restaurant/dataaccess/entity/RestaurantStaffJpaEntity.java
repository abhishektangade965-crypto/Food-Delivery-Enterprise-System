package com.fooddelivery.restaurant.dataaccess.entity;

import com.fooddelivery.restaurant.domain.valueobject.StaffRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "restaurant_staff")
@Entity
public class RestaurantStaffJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantJpaEntity restaurant;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffRole role;

    private LocalTime shiftStart;
    private LocalTime shiftEnd;

    @Column(nullable = false)
    private boolean isActive;
}
