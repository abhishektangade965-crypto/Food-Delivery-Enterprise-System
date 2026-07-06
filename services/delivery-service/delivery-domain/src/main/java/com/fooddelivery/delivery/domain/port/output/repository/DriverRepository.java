package com.fooddelivery.delivery.domain.port.output.repository;

import com.fooddelivery.delivery.domain.entity.Driver;
import com.fooddelivery.delivery.domain.valueobject.DriverId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository {
    Optional<Driver> findById(DriverId driverId);
    Optional<Driver> findByUserId(UUID userId);
    Driver save(Driver driver);
    List<Driver> findAvailableDrivers();
}
