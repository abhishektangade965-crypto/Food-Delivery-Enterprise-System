package com.fooddelivery.restaurant.domain.service;

import com.fooddelivery.restaurant.domain.entity.MenuCategory;
import com.fooddelivery.restaurant.domain.entity.Restaurant;
import com.fooddelivery.restaurant.domain.event.RestaurantApprovedEvent;
import com.fooddelivery.restaurant.domain.event.RestaurantMenuUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
public class RestaurantDomainServiceImpl implements RestaurantDomainService {

    @Override
    public void initializeRestaurant(Restaurant restaurant) {
        restaurant.initializeRestaurant();
        log.info("Initialized restaurant: {} with slug: {}", restaurant.getName(), restaurant.getSlug());
    }

    @Override
    public RestaurantApprovedEvent approveRestaurant(Restaurant restaurant) {
        restaurant.approve();
        log.info("Approved restaurant: {}", restaurant.getId().getValue());
        return new RestaurantApprovedEvent(restaurant, ZonedDateTime.now(ZoneId.of("UTC")));
    }

    @Override
    public void rejectRestaurant(Restaurant restaurant) {
        restaurant.reject();
        log.info("Rejected restaurant: {}", restaurant.getId().getValue());
    }

    @Override
    public RestaurantMenuUpdatedEvent updateMenu(Restaurant restaurant, List<MenuCategory> newCategories) {
        restaurant.updateMenu(newCategories);
        log.info("Updated menu for restaurant: {}", restaurant.getId().getValue());
        return new RestaurantMenuUpdatedEvent(restaurant, ZonedDateTime.now(ZoneId.of("UTC")));
    }

    @Override
    public void deductInventory(Restaurant restaurant, List<OrderItemDeduction> deductions) {
        if (deductions != null) {
            for (OrderItemDeduction deduction : deductions) {
                restaurant.deductInventory(deduction.inventoryItemId(), deduction.quantity());
            }
        }
        log.info("Deducted inventory for restaurant: {}", restaurant.getId().getValue());
    }

    @Override
    public BigDecimal calculateSurgeMultiplier(Restaurant restaurant, int activeOrders, double weatherFactor) {
        // Simple surge logic: 1.0 + (activeOrders * 0.03) + (weatherFactor * 0.15)
        BigDecimal base = BigDecimal.ONE;
        BigDecimal orderSurge = BigDecimal.valueOf(activeOrders).multiply(new BigDecimal("0.03"));
        BigDecimal weatherSurge = BigDecimal.valueOf(weatherFactor).multiply(new BigDecimal("0.15"));
        BigDecimal multiplier = base.add(orderSurge).add(weatherSurge).setScale(2, RoundingMode.HALF_UP);

        if (multiplier.compareTo(BigDecimal.ONE) < 0) {
            multiplier = BigDecimal.ONE;
        }

        restaurant.updateSurgeMultiplier(multiplier);
        log.info("Recalculated surge multiplier for restaurant {}: {}", restaurant.getId().getValue(), multiplier);
        return multiplier;
    }
}
