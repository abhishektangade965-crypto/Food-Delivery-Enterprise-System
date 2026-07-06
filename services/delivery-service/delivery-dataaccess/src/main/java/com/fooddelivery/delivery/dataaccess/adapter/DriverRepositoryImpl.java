package com.fooddelivery.delivery.dataaccess.adapter;

import com.fooddelivery.delivery.dataaccess.entity.DriverEntity;
import com.fooddelivery.delivery.dataaccess.mapper.DeliveryDataAccessMapper;
import com.fooddelivery.delivery.dataaccess.repository.DriverJpaRepository;
import com.fooddelivery.delivery.domain.entity.Driver;
import com.fooddelivery.delivery.domain.port.output.repository.DriverRepository;
import com.fooddelivery.delivery.domain.valueobject.DriverApprovalStatus;
import com.fooddelivery.delivery.domain.valueobject.DriverId;
import com.fooddelivery.delivery.domain.valueobject.DriverStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DriverRepositoryImpl implements DriverRepository {

    private final DriverJpaRepository driverJpaRepository;
    private final DeliveryDataAccessMapper deliveryDataAccessMapper;

    public DriverRepositoryImpl(DriverJpaRepository driverJpaRepository,
                                DeliveryDataAccessMapper deliveryDataAccessMapper) {
        this.driverJpaRepository = driverJpaRepository;
        this.deliveryDataAccessMapper = deliveryDataAccessMapper;
    }

    @Override
    public Optional<Driver> findById(DriverId driverId) {
        if (driverId == null || driverId.getValue() == null) {
            return Optional.empty();
        }
        return driverJpaRepository.findById(driverId.getValue())
                .map(deliveryDataAccessMapper::driverEntityToDriver);
    }

    @Override
    public Optional<Driver> findByUserId(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return driverJpaRepository.findByUserId(userId)
                .map(deliveryDataAccessMapper::driverEntityToDriver);
    }

    @Override
    public Driver save(Driver driver) {
        DriverEntity entity = deliveryDataAccessMapper.driverToDriverEntity(driver);
        DriverEntity saved = driverJpaRepository.save(entity);
        return deliveryDataAccessMapper.driverEntityToDriver(saved);
    }

    @Override
    public List<Driver> findAvailableDrivers() {
        return driverJpaRepository.findByStatusAndApprovalStatusAndIsActiveTrue(
                DriverStatus.ONLINE, DriverApprovalStatus.APPROVED)
                .stream()
                .map(deliveryDataAccessMapper::driverEntityToDriver)
                .collect(Collectors.toList());
    }
}
