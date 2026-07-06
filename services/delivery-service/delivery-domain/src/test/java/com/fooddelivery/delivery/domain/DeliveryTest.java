package com.fooddelivery.delivery.domain;

import com.fooddelivery.common.domain.valueobject.GeoLocation;
import com.fooddelivery.common.domain.valueobject.OrderId;
import com.fooddelivery.delivery.domain.entity.DeliveryAssignment;
import com.fooddelivery.delivery.domain.entity.Driver;
import com.fooddelivery.delivery.domain.valueobject.AssignmentStatus;
import com.fooddelivery.delivery.domain.valueobject.DriverApprovalStatus;
import com.fooddelivery.delivery.domain.valueobject.DriverStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryTest {

    @Test
    public void testDriverInitialization() {
        Driver driver = Driver.builder()
                .userId(UUID.randomUUID())
                .vehicleType("BIKE")
                .vehicleNumber("DL-1234")
                .build();

        driver.initializeDriver();

        assertNotNull(driver.getId());
        assertEquals(DriverStatus.OFFLINE, driver.getStatus());
        assertEquals(DriverApprovalStatus.PENDING, driver.getApprovalStatus());
        assertEquals(5.0, driver.getRating());
        assertEquals(BigDecimal.ZERO, driver.getWalletBalance());
        assertFalse(driver.getFaceVerified());
    }

    @Test
    public void testDeliveryAssignmentInitialization() {
        DeliveryAssignment assignment = DeliveryAssignment.builder()
                .orderId(new OrderId(UUID.randomUUID()))
                .pickupLocation(new GeoLocation(12.9715987, 77.5945627))
                .dropoffLocation(new GeoLocation(12.9562, 77.6015))
                .createdAt(ZonedDateTime.now())
                .build();

        assignment.initializeAssignment();

        assertNotNull(assignment.getId());
        assertEquals(AssignmentStatus.SEARCHING, assignment.getStatus());
        assertFalse(assignment.getOtpVerified());
        assertNotNull(assignment.getOtp());
        assertEquals(6, assignment.getOtp().length());
        assertEquals(BigDecimal.ZERO, assignment.getDeliveryFee());
    }
}
