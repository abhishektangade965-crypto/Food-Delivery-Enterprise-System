package com.fooddelivery.delivery.domain.service;

import com.fooddelivery.common.domain.valueobject.GeoLocation;
import com.fooddelivery.delivery.domain.entity.Driver;
import com.fooddelivery.delivery.domain.valueobject.DriverApprovalStatus;
import com.fooddelivery.delivery.domain.valueobject.DriverId;
import com.fooddelivery.delivery.domain.valueobject.DriverStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DeliveryDomainServiceImpl implements DeliveryDomainService {

    @Override
    public Optional<Driver> selectBestDriver(
            List<Driver> drivers,
            Map<DriverId, Integer> activeAssignmentsCount,
            GeoLocation pickupLocation) {
        if (drivers == null || drivers.isEmpty()) {
            return Optional.empty();
        }

        return drivers.stream()
                .filter(driver -> driver.getApprovalStatus() == DriverApprovalStatus.APPROVED)
                .filter(Driver::getIsActive)
                .filter(driver -> driver.getStatus() != DriverStatus.OFFLINE)
                .max(Comparator.comparingDouble(driver -> calculateDriverScore(driver, activeAssignmentsCount, pickupLocation)));
    }

    private double calculateDriverScore(
            Driver driver,
            Map<DriverId, Integer> activeAssignmentsCount,
            GeoLocation pickupLocation) {
        
        double rating = driver.getRating() != null ? driver.getRating() : 5.0;
        
        double distance = 50.0; // Default large distance if location is missing
        if (driver.getLocation() != null && pickupLocation != null) {
            distance = driver.getLocation().distanceInKilometers(pickupLocation);
        }

        int activeCount = activeAssignmentsCount.getOrDefault(driver.getId(), 0);

        // Score formula: high rating is positive, high distance is negative, high active load is negative
        double score = (rating * 15.0) - (distance * 5.0) - (activeCount * 12.0);

        // Additional penalty if driver status is BUSY
        if (driver.getStatus() == DriverStatus.BUSY) {
            score -= 25.0;
        }

        return score;
    }
}
