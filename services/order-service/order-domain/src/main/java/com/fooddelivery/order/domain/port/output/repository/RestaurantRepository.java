package com.fooddelivery.order.domain.port.output.repository;

import com.fooddelivery.order.domain.entity.Restaurant;

import java.util.Optional;

public interface RestaurantRepository {
    Optional<Restaurant> findRestaurantInformation(Restaurant restaurant);
}
