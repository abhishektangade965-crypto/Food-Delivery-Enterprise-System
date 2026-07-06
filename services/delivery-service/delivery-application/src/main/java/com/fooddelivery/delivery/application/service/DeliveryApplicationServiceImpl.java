package com.fooddelivery.delivery.application.service;

import com.fooddelivery.common.domain.exception.DomainException;
import com.fooddelivery.common.domain.exception.DomainNotFoundException;
import com.fooddelivery.common.domain.valueobject.OrderId;
import com.fooddelivery.delivery.application.dto.*;
import com.fooddelivery.delivery.application.mapper.DeliveryDataMapper;
import com.fooddelivery.delivery.domain.entity.DeliveryAssignment;
import com.fooddelivery.delivery.domain.entity.Driver;
import com.fooddelivery.delivery.domain.port.output.message.publisher.DeliveryEventPublisher;
import com.fooddelivery.delivery.domain.port.output.repository.DeliveryAssignmentRepository;
import com.fooddelivery.delivery.domain.port.output.repository.DriverRepository;
import com.fooddelivery.delivery.domain.service.DeliveryDomainService;
import com.fooddelivery.delivery.domain.service.DeliveryDomainServiceImpl;
import com.fooddelivery.delivery.domain.valueobject.AssignmentId;
import com.fooddelivery.delivery.domain.valueobject.AssignmentStatus;
import com.fooddelivery.delivery.domain.valueobject.DriverApprovalStatus;
import com.fooddelivery.delivery.domain.valueobject.DriverId;
import com.fooddelivery.delivery.domain.valueobject.DriverStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DeliveryApplicationServiceImpl implements DeliveryApplicationService {

    private final DriverRepository driverRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final DeliveryDataMapper deliveryDataMapper;
    private final DeliveryDomainService deliveryDomainService = new DeliveryDomainServiceImpl();
    private final DeliveryEventPublisher deliveryEventPublisher;

    public DeliveryApplicationServiceImpl(DriverRepository driverRepository,
                                           DeliveryAssignmentRepository deliveryAssignmentRepository,
                                           DeliveryDataMapper deliveryDataMapper,
                                           DeliveryEventPublisher deliveryEventPublisher) {
        this.driverRepository = driverRepository;
        this.deliveryAssignmentRepository = deliveryAssignmentRepository;
        this.deliveryDataMapper = deliveryDataMapper;
        this.deliveryEventPublisher = deliveryEventPublisher;
    }

    @Override
    @Transactional
    public DriverResponseDto registerDriver(DriverRegistrationDto registrationDto) {
        log.info("Registering driver for user: {}", registrationDto.getUserId());
        Optional<Driver> existingDriver = driverRepository.findByUserId(registrationDto.getUserId());
        if (existingDriver.isPresent()) {
            throw new DomainException("Driver with user ID " + registrationDto.getUserId() + " already exists");
        }

        Driver driver = deliveryDataMapper.driverRegistrationDtoToDriver(registrationDto);
        driver.initializeDriver();
        driver.setApprovalStatus(DriverApprovalStatus.APPROVED);
        driver.setIsActive(true);

        Driver saved = driverRepository.save(driver);
        return deliveryDataMapper.driverToDriverResponseDto(saved);
    }

    @Override
    @Transactional
    public DriverResponseDto startShift(UUID driverId) {
        log.info("Starting shift for driver: {}", driverId);
        Driver driver = driverRepository.findById(new DriverId(driverId))
                .orElseThrow(() -> new DomainNotFoundException("Driver not found with ID: " + driverId));

        if (driver.getApprovalStatus() != DriverApprovalStatus.APPROVED) {
            throw new DomainException("Driver cannot start shift. Approval status: " + driver.getApprovalStatus());
        }

        driver.setStatus(DriverStatus.ONLINE);
        Driver saved = driverRepository.save(driver);

        assignPendingDeliveries();

        return deliveryDataMapper.driverToDriverResponseDto(saved);
    }

    @Override
    @Transactional
    public DriverResponseDto endShift(UUID driverId) {
        log.info("Ending shift for driver: {}", driverId);
        Driver driver = driverRepository.findById(new DriverId(driverId))
                .orElseThrow(() -> new DomainNotFoundException("Driver not found with ID: " + driverId));

        driver.setStatus(DriverStatus.OFFLINE);
        Driver saved = driverRepository.save(driver);
        return deliveryDataMapper.driverToDriverResponseDto(saved);
    }

    @Override
    @Transactional
    public DriverResponseDto updateLocation(UUID driverId, LocationDto locationDto) {
        log.info("Updating location for driver: {} to lat: {}, lon: {}", driverId, locationDto.getLatitude(), locationDto.getLongitude());
        Driver driver = driverRepository.findById(new DriverId(driverId))
                .orElseThrow(() -> new DomainNotFoundException("Driver not found with ID: " + driverId));

        driver.setLocation(deliveryDataMapper.locationDtoToGeoLocation(locationDto));
        Driver saved = driverRepository.save(driver);

        if (driver.getStatus() == DriverStatus.ONLINE) {
            assignPendingDeliveries();
        }

        return deliveryDataMapper.driverToDriverResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponseDto getAssignmentStatus(UUID assignmentId) {
        log.info("Fetching assignment status for ID: {}", assignmentId);
        DeliveryAssignment assignment = deliveryAssignmentRepository.findById(new AssignmentId(assignmentId))
                .orElseThrow(() -> new DomainNotFoundException("Delivery assignment not found with ID: " + assignmentId));
        return deliveryDataMapper.deliveryAssignmentToAssignmentResponseDto(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponseDto updateAssignmentStatus(UUID assignmentId, String status) {
        log.info("Updating assignment status for ID: {} to {}", assignmentId, status);
        DeliveryAssignment assignment = deliveryAssignmentRepository.findById(new AssignmentId(assignmentId))
                .orElseThrow(() -> new DomainNotFoundException("Delivery assignment not found with ID: " + assignmentId));

        AssignmentStatus newStatus = AssignmentStatus.valueOf(status.toUpperCase());
        assignment.setStatus(newStatus);

        if (newStatus == AssignmentStatus.PICKED_UP) {
            assignment.setActualPickupTime(ZonedDateTime.now());
            assignment.setStatus(AssignmentStatus.DELIVERING);
        } else if (newStatus == AssignmentStatus.DELIVERED) {
            assignment.setActualDeliveryTime(ZonedDateTime.now());
            releaseDriver(assignment.getDriverId());
        } else if (newStatus == AssignmentStatus.FAILED) {
            releaseDriver(assignment.getDriverId());
        }

        DeliveryAssignment saved = deliveryAssignmentRepository.save(assignment);
        deliveryEventPublisher.publish(saved);

        return deliveryDataMapper.deliveryAssignmentToAssignmentResponseDto(saved);
    }

    @Override
    @Transactional
    public AssignmentResponseDto verifyOtp(UUID assignmentId, String otp) {
        log.info("Verifying OTP for assignment ID: {}", assignmentId);
        DeliveryAssignment assignment = deliveryAssignmentRepository.findById(new AssignmentId(assignmentId))
                .orElseThrow(() -> new DomainNotFoundException("Delivery assignment not found with ID: " + assignmentId));

        if (assignment.getOtp() == null || !assignment.getOtp().equals(otp)) {
            throw new DomainException("Invalid OTP verification code");
        }

        assignment.setOtpVerified(true);
        assignment.setStatus(AssignmentStatus.DELIVERED);
        assignment.setActualDeliveryTime(ZonedDateTime.now());

        if (assignment.getDriverId() != null) {
            Driver driver = driverRepository.findById(assignment.getDriverId()).orElse(null);
            if (driver != null) {
                BigDecimal payout = assignment.getDeliveryFee().add(assignment.getTipAmount());
                driver.setWalletBalance(driver.getWalletBalance().add(payout));
                driver.setTotalDeliveries(driver.getTotalDeliveries() + 1);
                driver.setStatus(DriverStatus.ONLINE);
                driverRepository.save(driver);
            }
        }

        DeliveryAssignment saved = deliveryAssignmentRepository.save(assignment);
        deliveryEventPublisher.publish(saved);

        return deliveryDataMapper.deliveryAssignmentToAssignmentResponseDto(saved);
    }

    @Override
    @Transactional
    public AssignmentResponseDto uploadProofOfDelivery(UUID assignmentId, String proofOfDeliveryUrl) {
        log.info("Uploading proof of delivery for assignment ID: {}", assignmentId);
        DeliveryAssignment assignment = deliveryAssignmentRepository.findById(new AssignmentId(assignmentId))
                .orElseThrow(() -> new DomainNotFoundException("Delivery assignment not found with ID: " + assignmentId));

        assignment.setProofOfDeliveryUrl(proofOfDeliveryUrl);
        assignment.setStatus(AssignmentStatus.DELIVERED);
        assignment.setActualDeliveryTime(ZonedDateTime.now());

        if (assignment.getDriverId() != null) {
            Driver driver = driverRepository.findById(assignment.getDriverId()).orElse(null);
            if (driver != null) {
                BigDecimal payout = assignment.getDeliveryFee().add(assignment.getTipAmount());
                driver.setWalletBalance(driver.getWalletBalance().add(payout));
                driver.setTotalDeliveries(driver.getTotalDeliveries() + 1);
                driver.setStatus(DriverStatus.ONLINE);
                driverRepository.save(driver);
            }
        }

        DeliveryAssignment saved = deliveryAssignmentRepository.save(assignment);
        deliveryEventPublisher.publish(saved);

        return deliveryDataMapper.deliveryAssignmentToAssignmentResponseDto(saved);
    }

    @Override
    @Transactional
    public void assignDriverForOrder(UUID orderId, LocationDto pickupLocation, LocationDto dropoffLocation, 
                                      double distanceKm, BigDecimal deliveryFee, BigDecimal tipAmount) {
        log.info("Assigning driver for order: {}", orderId);
        
        Optional<DeliveryAssignment> existing = deliveryAssignmentRepository.findByOrderId(new OrderId(orderId));
        if (existing.isPresent()) {
            log.warn("Delivery assignment already exists for order: {}", orderId);
            return;
        }

        DeliveryAssignment assignment = DeliveryAssignment.builder()
                .orderId(new OrderId(orderId))
                .pickupLocation(deliveryDataMapper.locationDtoToGeoLocation(pickupLocation))
                .dropoffLocation(deliveryDataMapper.locationDtoToGeoLocation(dropoffLocation))
                .distanceKm(distanceKm)
                .deliveryFee(deliveryFee)
                .tipAmount(tipAmount)
                .createdAt(ZonedDateTime.now())
                .estimatedPickupTime(ZonedDateTime.now().plusMinutes(15))
                .estimatedDeliveryTime(ZonedDateTime.now().plusMinutes(45))
                .build();
        assignment.initializeAssignment();

        List<Driver> availableDrivers = driverRepository.findAvailableDrivers();
        Map<DriverId, Integer> activeCounts = getActiveAssignmentsCountMap();

        Optional<Driver> selectedDriver = deliveryDomainService.selectBestDriver(
                availableDrivers,
                activeCounts,
                assignment.getPickupLocation()
        );

        if (selectedDriver.isPresent()) {
            Driver driver = selectedDriver.get();
            assignment.setDriverId(driver.getId());
            assignment.setStatus(AssignmentStatus.ASSIGNED);
            driver.setStatus(DriverStatus.BUSY);
            
            driverRepository.save(driver);
            log.info("Successfully assigned driver {} to order {}", driver.getId().getValue(), orderId);
        } else {
            assignment.setStatus(AssignmentStatus.SEARCHING);
            log.warn("No available drivers found for order {}. Set to SEARCHING.", orderId);
        }

        DeliveryAssignment saved = deliveryAssignmentRepository.save(assignment);
        deliveryEventPublisher.publish(saved);
    }

    private void assignPendingDeliveries() {
        List<DeliveryAssignment> searchingAssignments = deliveryAssignmentRepository.findAllActiveAssignments().stream()
                .filter(a -> a.getStatus() == AssignmentStatus.SEARCHING)
                .collect(Collectors.toList());

        if (searchingAssignments.isEmpty()) {
            return;
        }

        List<Driver> availableDrivers = driverRepository.findAvailableDrivers();
        if (availableDrivers.isEmpty()) {
            return;
        }

        Map<DriverId, Integer> activeCounts = getActiveAssignmentsCountMap();

        for (DeliveryAssignment assignment : searchingAssignments) {
            Optional<Driver> selectedDriver = deliveryDomainService.selectBestDriver(
                    availableDrivers,
                    activeCounts,
                    assignment.getPickupLocation()
            );

            if (selectedDriver.isPresent()) {
                Driver driver = selectedDriver.get();
                assignment.setDriverId(driver.getId());
                assignment.setStatus(AssignmentStatus.ASSIGNED);
                driver.setStatus(DriverStatus.BUSY);

                driverRepository.save(driver);
                deliveryAssignmentRepository.save(assignment);
                deliveryEventPublisher.publish(assignment);

                log.info("Late-assigned driver {} to order {}", driver.getId().getValue(), assignment.getOrderId().getValue());

                availableDrivers.remove(driver);
                activeCounts.put(driver.getId(), activeCounts.getOrDefault(driver.getId(), 0) + 1);
            }
        }
    }

    private Map<DriverId, Integer> getActiveAssignmentsCountMap() {
        List<DeliveryAssignment> activeAssignments = deliveryAssignmentRepository.findAllActiveAssignments();
        Map<DriverId, Integer> activeCounts = new HashMap<>();
        for (DeliveryAssignment assignment : activeAssignments) {
            if (assignment.getDriverId() != null) {
                activeCounts.merge(assignment.getDriverId(), 1, Integer::sum);
            }
        }
        return activeCounts;
    }

    private void releaseDriver(DriverId driverId) {
        if (driverId != null) {
            Driver driver = driverRepository.findById(driverId).orElse(null);
            if (driver != null) {
                driver.setStatus(DriverStatus.ONLINE);
                driverRepository.save(driver);
            }
        }
    }
}
