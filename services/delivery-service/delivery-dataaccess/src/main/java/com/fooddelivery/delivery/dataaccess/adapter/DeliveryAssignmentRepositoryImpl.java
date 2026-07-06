package com.fooddelivery.delivery.dataaccess.adapter;

import com.fooddelivery.common.domain.valueobject.OrderId;
import com.fooddelivery.delivery.dataaccess.entity.DeliveryAssignmentEntity;
import com.fooddelivery.delivery.dataaccess.mapper.DeliveryDataAccessMapper;
import com.fooddelivery.delivery.dataaccess.repository.DeliveryAssignmentJpaRepository;
import com.fooddelivery.delivery.domain.entity.DeliveryAssignment;
import com.fooddelivery.delivery.domain.port.output.repository.DeliveryAssignmentRepository;
import com.fooddelivery.delivery.domain.valueobject.AssignmentId;
import com.fooddelivery.delivery.domain.valueobject.AssignmentStatus;
import com.fooddelivery.delivery.domain.valueobject.DriverId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DeliveryAssignmentRepositoryImpl implements DeliveryAssignmentRepository {

    private final DeliveryAssignmentJpaRepository deliveryAssignmentJpaRepository;
    private final DeliveryDataAccessMapper deliveryDataAccessMapper;
    
    private static final List<AssignmentStatus> INACTIVE_STATUSES = List.of(
            AssignmentStatus.DELIVERED, 
            AssignmentStatus.FAILED
    );

    public DeliveryAssignmentRepositoryImpl(DeliveryAssignmentJpaRepository deliveryAssignmentJpaRepository,
                                            DeliveryDataAccessMapper deliveryDataAccessMapper) {
        this.deliveryAssignmentJpaRepository = deliveryAssignmentJpaRepository;
        this.deliveryDataAccessMapper = deliveryDataAccessMapper;
    }

    @Override
    public Optional<DeliveryAssignment> findById(AssignmentId assignmentId) {
        if (assignmentId == null || assignmentId.getValue() == null) {
            return Optional.empty();
        }
        return deliveryAssignmentJpaRepository.findById(assignmentId.getValue())
                .map(deliveryDataAccessMapper::deliveryAssignmentEntityToDeliveryAssignment);
    }

    @Override
    public Optional<DeliveryAssignment> findByOrderId(OrderId orderId) {
        if (orderId == null || orderId.getValue() == null) {
            return Optional.empty();
        }
        return deliveryAssignmentJpaRepository.findByOrderId(orderId.getValue())
                .map(deliveryDataAccessMapper::deliveryAssignmentEntityToDeliveryAssignment);
    }

    @Override
    public DeliveryAssignment save(DeliveryAssignment deliveryAssignment) {
        DeliveryAssignmentEntity entity = deliveryDataAccessMapper.deliveryAssignmentToDeliveryAssignmentEntity(deliveryAssignment);
        DeliveryAssignmentEntity saved = deliveryAssignmentJpaRepository.save(entity);
        return deliveryDataAccessMapper.deliveryAssignmentEntityToDeliveryAssignment(saved);
    }

    @Override
    public List<DeliveryAssignment> findActiveAssignmentsByDriverId(DriverId driverId) {
        if (driverId == null || driverId.getValue() == null) {
            return List.of();
        }
        return deliveryAssignmentJpaRepository.findByDriverIdAndStatusNotIn(driverId.getValue(), INACTIVE_STATUSES)
                .stream()
                .map(deliveryDataAccessMapper::deliveryAssignmentEntityToDeliveryAssignment)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryAssignment> findAllActiveAssignments() {
        return deliveryAssignmentJpaRepository.findByStatusNotIn(INACTIVE_STATUSES)
                .stream()
                .map(deliveryDataAccessMapper::deliveryAssignmentEntityToDeliveryAssignment)
                .collect(Collectors.toList());
    }
}
