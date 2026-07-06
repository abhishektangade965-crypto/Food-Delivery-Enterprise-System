package com.fooddelivery.restaurant.domain.entity;

import com.fooddelivery.common.domain.entity.BaseEntity;
import com.fooddelivery.restaurant.domain.valueobject.StaffRole;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantStaff extends BaseEntity<UUID> {
    private UUID userId;
    private StaffRole role;
    private LocalTime shiftStart;
    private LocalTime shiftEnd;
    private boolean isActive;
}
