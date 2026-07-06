package com.fooddelivery.order.dataaccess.adapter;

import com.fooddelivery.order.dataaccess.mapper.OrderDataAccessMapper;
import com.fooddelivery.order.dataaccess.repository.RestaurantJpaRepository;
import com.fooddelivery.order.domain.entity.Restaurant;
import com.fooddelivery.order.domain.port.output.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RestaurantRepositoryImpl implements RestaurantRepository {

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final OrderDataAccessMapper orderDataAccessMapper;

    @Override
    public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
        return restaurantJpaRepository.findById(restaurant.getId().getValue())
                .map(orderDataAccessMapper::restaurantEntityToRestaurant);
    }
}
