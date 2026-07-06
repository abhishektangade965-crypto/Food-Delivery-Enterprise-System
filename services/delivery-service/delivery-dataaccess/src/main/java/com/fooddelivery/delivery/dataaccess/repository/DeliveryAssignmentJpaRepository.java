package com.fooddelivery.delivery.dataaccess.repository;

import com.fooddelivery.delivery.dataaccess.entity.DeliveryAssignmentEntity;
import com.fooddelivery.delivery.domain.valueobject.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryAssignmentJpaRepository extends JpaRepository<DeliveryAssignmentEntity, UUID> {
    Optional<DeliveryAssignmentEntity> findByOrderId(UUID orderId);
    
    @Query("SELECT a FROM DeliveryAssignmentEntity a WHERE a.status NOT IN (:statuses)")
    List<DeliveryAssignmentEntity> findByStatusNotIn(@Param("statuses") Collection<AssignmentStatus> statuses);
    
    @Query("SELECT a FROM DeliveryAssignmentEntity a WHERE a.driverId = :driverId AND a.status NOT IN (:statuses)")
    List<DeliveryAssignmentEntity> findByDriverIdAndStatusNotIn(
            @Param("driverId") UUID driverId,
            @Param("statuses") Collection<AssignmentStatus> statuses
    );
}
