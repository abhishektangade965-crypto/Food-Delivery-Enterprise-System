package com.fooddelivery.restaurant.domain.port.output.repository;

import com.fooddelivery.restaurant.domain.entity.Restaurant;
import com.fooddelivery.restaurant.domain.valueobject.RestaurantId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository {
    Restaurant save(Restaurant restaurant);
    Optional<Restaurant> findById(RestaurantId restaurantId);
    Optional<Restaurant> findBySlug(String slug);
    List<Restaurant> findAll();
    List<Restaurant> findActiveNear(double latitude, double longitude, double radiusKm);
    List<Restaurant> findByOwnerId(UUID ownerId);
    void delete(RestaurantId restaurantId);
}
