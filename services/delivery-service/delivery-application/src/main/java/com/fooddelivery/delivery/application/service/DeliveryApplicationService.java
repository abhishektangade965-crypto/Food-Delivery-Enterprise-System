package com.fooddelivery.delivery.application.service;

import com.fooddelivery.delivery.application.dto.*;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryApplicationService {
    DriverResponseDto registerDriver(DriverRegistrationDto registrationDto);
    DriverResponseDto startShift(UUID driverId);
    DriverResponseDto endShift(UUID driverId);
    DriverResponseDto updateLocation(UUID driverId, LocationDto locationDto);
    AssignmentResponseDto getAssignmentStatus(UUID assignmentId);
    AssignmentResponseDto updateAssignmentStatus(UUID assignmentId, String status);
    AssignmentResponseDto verifyOtp(UUID assignmentId, String otp);
    AssignmentResponseDto uploadProofOfDelivery(UUID assignmentId, String proofOfDeliveryUrl);
    
    void assignDriverForOrder(UUID orderId, LocationDto pickupLocation, LocationDto dropoffLocation, 
                              double distanceKm, BigDecimal deliveryFee, BigDecimal tipAmount);
}
