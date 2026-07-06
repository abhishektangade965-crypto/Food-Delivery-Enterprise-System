package com.fooddelivery.delivery.application.rest;

import com.fooddelivery.delivery.application.dto.*;
import com.fooddelivery.delivery.application.service.DeliveryApplicationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/deliveries")
public class DeliveryController {

    private final DeliveryApplicationService deliveryApplicationService;

    public DeliveryController(DeliveryApplicationService deliveryApplicationService) {
        this.deliveryApplicationService = deliveryApplicationService;
    }

    @PostMapping("/drivers/register")
    public ResponseEntity<DriverResponseDto> registerDriver(@Valid @RequestBody DriverRegistrationDto registrationDto) {
        log.info("REST request to register driver for user: {}", registrationDto.getUserId());
        DriverResponseDto response = deliveryApplicationService.registerDriver(registrationDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/drivers/{driverId}/shift/start")
    public ResponseEntity<DriverResponseDto> startShift(@PathVariable UUID driverId) {
        log.info("REST request to start shift for driver: {}", driverId);
        DriverResponseDto response = deliveryApplicationService.startShift(driverId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/drivers/{driverId}/shift/end")
    public ResponseEntity<DriverResponseDto> endShift(@PathVariable UUID driverId) {
        log.info("REST request to end shift for driver: {}", driverId);
        DriverResponseDto response = deliveryApplicationService.endShift(driverId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/drivers/{driverId}/location")
    public ResponseEntity<DriverResponseDto> updateLocation(
            @PathVariable UUID driverId,
            @Valid @RequestBody LocationDto locationDto) {
        log.info("REST request to update location for driver: {}", driverId);
        DriverResponseDto response = deliveryApplicationService.updateLocation(driverId, locationDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignments/{assignmentId}")
    public ResponseEntity<AssignmentResponseDto> getAssignmentStatus(@PathVariable UUID assignmentId) {
        log.info("REST request to get status for assignment: {}", assignmentId);
        AssignmentResponseDto response = deliveryApplicationService.getAssignmentStatus(assignmentId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/assignments/{assignmentId}/status")
    public ResponseEntity<AssignmentResponseDto> updateAssignmentStatus(
            @PathVariable UUID assignmentId,
            @RequestParam String status) {
        log.info("REST request to update status for assignment: {} to {}", assignmentId, status);
        AssignmentResponseDto response = deliveryApplicationService.updateAssignmentStatus(assignmentId, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assignments/{assignmentId}/verify-otp")
    public ResponseEntity<AssignmentResponseDto> verifyOtp(
            @PathVariable UUID assignmentId,
            @RequestParam String otp) {
        log.info("REST request to verify OTP for assignment: {}", assignmentId);
        AssignmentResponseDto response = deliveryApplicationService.verifyOtp(assignmentId, otp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assignments/{assignmentId}/proof")
    public ResponseEntity<AssignmentResponseDto> uploadProofOfDelivery(
            @PathVariable UUID assignmentId,
            @RequestParam String proofOfDeliveryUrl) {
        log.info("REST request to upload proof of delivery for assignment: {}", assignmentId);
        AssignmentResponseDto response = deliveryApplicationService.uploadProofOfDelivery(assignmentId, proofOfDeliveryUrl);
        return ResponseEntity.ok(response);
    }
}
