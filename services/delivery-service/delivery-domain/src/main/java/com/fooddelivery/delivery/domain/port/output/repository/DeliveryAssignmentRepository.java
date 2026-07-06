package com.fooddelivery.delivery.domain.port.output.repository;

import com.fooddelivery.common.domain.valueobject.OrderId;
import com.fooddelivery.delivery.domain.entity.DeliveryAssignment;
import com.fooddelivery.delivery.domain.valueobject.AssignmentId;
import com.fooddelivery.delivery.domain.valueobject.DriverId;

import java.util.List;
import java.util.Optional;

public interface DeliveryAssignmentRepository {
    Optional<DeliveryAssignment> findById(AssignmentId assignmentId);
    Optional<DeliveryAssignment> findByOrderId(OrderId orderId);
    DeliveryAssignment save(DeliveryAssignment deliveryAssignment);
    List<DeliveryAssignment> findActiveAssignmentsByDriverId(DriverId driverId);
    List<DeliveryAssignment> findAllActiveAssignments();
}
