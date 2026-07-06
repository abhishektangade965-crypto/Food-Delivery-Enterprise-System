package com.fooddelivery.restaurant.application.service;

import com.fooddelivery.restaurant.application.dto.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface RestaurantApplicationService {
    RestaurantResponse createRestaurant(CreateRestaurantRequest request);
    RestaurantResponse updateRestaurant(UUID id, UpdateRestaurantRequest request);
    RestaurantResponse getRestaurantById(UUID id);
    RestaurantResponse getRestaurantBySlug(String slug);
    List<RestaurantResponse> getAllRestaurants();
    List<RestaurantResponse> findActiveNear(double latitude, double longitude, double radiusKm);
    RestaurantResponse updateMenu(UUID id, UpdateMenuRequest request);
    RestaurantResponse approveRestaurant(UUID id);
    RestaurantResponse rejectRestaurant(UUID id);
    BigDecimal updateSurgeMultiplier(UUID id, int activeOrders, double weatherFactor);
}
