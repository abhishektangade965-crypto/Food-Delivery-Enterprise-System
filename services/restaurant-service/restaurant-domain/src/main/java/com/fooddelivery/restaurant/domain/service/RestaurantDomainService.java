package com.fooddelivery.restaurant.domain.service;

import com.fooddelivery.restaurant.domain.entity.MenuCategory;
import com.fooddelivery.restaurant.domain.entity.Restaurant;
import com.fooddelivery.restaurant.domain.event.RestaurantApprovedEvent;
import com.fooddelivery.restaurant.domain.event.RestaurantMenuUpdatedEvent;

import java.math.BigDecimal;
import java.util.List;

public interface RestaurantDomainService {
    void initializeRestaurant(Restaurant restaurant);
    RestaurantApprovedEvent approveRestaurant(Restaurant restaurant);
    void rejectRestaurant(Restaurant restaurant);
    RestaurantMenuUpdatedEvent updateMenu(Restaurant restaurant, List<MenuCategory> newCategories);
    void deductInventory(Restaurant restaurant, List<OrderItemDeduction> deductions);
    BigDecimal calculateSurgeMultiplier(Restaurant restaurant, int activeOrders, double weatherFactor);
}
