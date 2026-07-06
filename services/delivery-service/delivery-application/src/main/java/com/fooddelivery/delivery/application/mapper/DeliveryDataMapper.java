package com.fooddelivery.delivery.application.mapper;

import com.fooddelivery.common.domain.valueobject.GeoLocation;
import com.fooddelivery.delivery.application.dto.*;
import com.fooddelivery.delivery.domain.entity.DeliveryAssignment;
import com.fooddelivery.delivery.domain.entity.Driver;
import org.springframework.stereotype.Component;

@Component
public class DeliveryDataMapper {

    public Driver driverRegistrationDtoToDriver(DriverRegistrationDto dto) {
        return Driver.builder()
                .userId(dto.getUserId())
                .vehicleType(dto.getVehicleType())
                .vehicleNumber(dto.getVehicleNumber())
                .licenseNumber(dto.getLicenseNumber())
                .build();
    }

    public DriverResponseDto driverToDriverResponseDto(Driver driver) {
        if (driver == null) return null;
        return DriverResponseDto.builder()
                .driverId(driver.getId() != null ? driver.getId().getValue() : null)
                .userId(driver.getUserId())
                .vehicleType(driver.getVehicleType())
                .vehicleNumber(driver.getVehicleNumber())
                .licenseNumber(driver.getLicenseNumber())
                .status(driver.getStatus() != null ? driver.getStatus().name() : null)
                .approvalStatus(driver.getApprovalStatus() != null ? driver.getApprovalStatus().name() : null)
                .rating(driver.getRating())
                .totalDeliveries(driver.getTotalDeliveries())
                .acceptanceRate(driver.getAcceptanceRate())
                .walletBalance(driver.getWalletBalance())
                .faceVerified(driver.getFaceVerified())
                .isActive(driver.getIsActive())
                .location(geoLocationToLocationDto(driver.getLocation()))
                .build();
    }

    public LocationDto geoLocationToLocationDto(GeoLocation location) {
        if (location == null) return null;
        return LocationDto.builder()
                .latitude(location.latitude())
                .longitude(location.longitude())
                .build();
    }

    public GeoLocation locationDtoToGeoLocation(LocationDto dto) {
        if (dto == null) return null;
        return GeoLocation.of(dto.getLatitude(), dto.getLongitude());
    }

    public AssignmentResponseDto deliveryAssignmentToAssignmentResponseDto(DeliveryAssignment assignment) {
        if (assignment == null) return null;
        return AssignmentResponseDto.builder()
                .assignmentId(assignment.getId() != null ? assignment.getId().getValue() : null)
                .orderId(assignment.getOrderId() != null ? assignment.getOrderId().getValue() : null)
                .driverId(assignment.getDriverId() != null ? assignment.getDriverId().getValue() : null)
                .batchId(assignment.getBatchId())
                .status(assignment.getStatus() != null ? assignment.getStatus().name() : null)
                .pickupLocation(geoLocationToLocationDto(assignment.getPickupLocation()))
                .dropoffLocation(geoLocationToLocationDto(assignment.getDropoffLocation()))
                .estimatedPickupTime(assignment.getEstimatedPickupTime())
                .estimatedDeliveryTime(assignment.getEstimatedDeliveryTime())
                .actualPickupTime(assignment.getActualPickupTime())
                .actualDeliveryTime(assignment.getActualDeliveryTime())
                .distanceKm(assignment.getDistanceKm())
                .otp(assignment.getOtp())
                .otpVerified(assignment.getOtpVerified())
                .proofOfDeliveryUrl(assignment.getProofOfDeliveryUrl())
                .driverNotes(assignment.getDriverNotes())
                .deliveryFee(assignment.getDeliveryFee())
                .tipAmount(assignment.getTipAmount())
                .createdAt(assignment.getCreatedAt())
                .build();
    }
}
