package com.fooddelivery.restaurant.domain;

import com.fooddelivery.restaurant.domain.entity.Restaurant;
import com.fooddelivery.restaurant.domain.valueobject.ApprovalStatus;
import com.fooddelivery.restaurant.domain.valueobject.RestaurantStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class RestaurantTest {

    @Test
    public void testRestaurantInitialization() {
        Restaurant restaurant = Restaurant.builder()
                .name("Bella Italia")
                .slug("bella-italia")
                .build();

        restaurant.initializeRestaurant();

        assertNotNull(restaurant.getId());
        assertEquals(RestaurantStatus.PENDING_APPROVAL, restaurant.getStatus());
        assertEquals(ApprovalStatus.PENDING, restaurant.getApprovalStatus());
        assertFalse(restaurant.isActive());
        assertEquals(BigDecimal.ZERO, restaurant.getRating());
    }

    @Test
    public void testRestaurantApproval() {
        Restaurant restaurant = Restaurant.builder()
                .name("Bella Italia")
                .build();
        restaurant.initializeRestaurant();

        restaurant.approve();

        assertEquals(RestaurantStatus.ACTIVE, restaurant.getStatus());
        assertEquals(ApprovalStatus.APPROVED, restaurant.getApprovalStatus());
        assertTrue(restaurant.isActive());
    }

    @Test
    public void testRestaurantRejection() {
        Restaurant restaurant = Restaurant.builder()
                .name("Bella Italia")
                .build();
        restaurant.initializeRestaurant();

        restaurant.reject();

        assertEquals(RestaurantStatus.INACTIVE, restaurant.getStatus());
        assertEquals(ApprovalStatus.REJECTED, restaurant.getApprovalStatus());
        assertFalse(restaurant.isActive());
    }

    @Test
    public void testRestaurantSuspension() {
        Restaurant restaurant = Restaurant.builder()
                .name("Bella Italia")
                .build();
        restaurant.initializeRestaurant();
        restaurant.approve();

        restaurant.suspend();

        assertEquals(RestaurantStatus.SUSPENDED, restaurant.getStatus());
        assertFalse(restaurant.isActive());
    }

    @Test
    public void testRestaurantSurgeMultiplier() {
        Restaurant restaurant = Restaurant.builder()
                .name("Bella Italia")
                .build();
        restaurant.initializeRestaurant();

        restaurant.updateSurgeMultiplier(new BigDecimal("1.5"));
        assertEquals(new BigDecimal("1.5"), restaurant.getSurgeMultiplier());

        assertThrows(IllegalArgumentException.class, () -> restaurant.updateSurgeMultiplier(new BigDecimal("0.9")));
    }
}
