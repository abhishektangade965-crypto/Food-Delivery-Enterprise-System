package com.fooddelivery.delivery.dataaccess.repository;

import com.fooddelivery.delivery.dataaccess.entity.DriverEntity;
import com.fooddelivery.delivery.domain.valueobject.DriverApprovalStatus;
import com.fooddelivery.delivery.domain.valueobject.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverJpaRepository extends JpaRepository<DriverEntity, UUID> {
    Optional<DriverEntity> findByUserId(UUID userId);
    List<DriverEntity> findByStatusAndApprovalStatusAndIsActiveTrue(DriverStatus status, DriverApprovalStatus approvalStatus);
}
