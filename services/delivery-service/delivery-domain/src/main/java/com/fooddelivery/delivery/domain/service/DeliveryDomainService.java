package com.fooddelivery.delivery.domain.service;

import com.fooddelivery.common.domain.valueobject.GeoLocation;
import com.fooddelivery.delivery.domain.entity.Driver;
import com.fooddelivery.delivery.domain.valueobject.DriverId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DeliveryDomainService {
    Optional<Driver> selectBestDriver(
            List<Driver> drivers,
            Map<DriverId, Integer> activeAssignmentsCount,
            GeoLocation pickupLocation
    );
}
