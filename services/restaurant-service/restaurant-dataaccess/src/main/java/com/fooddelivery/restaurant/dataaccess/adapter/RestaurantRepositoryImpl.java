package com.fooddelivery.restaurant.dataaccess.adapter;

import com.fooddelivery.restaurant.dataaccess.entity.RestaurantJpaEntity;
import com.fooddelivery.restaurant.dataaccess.mapper.RestaurantDataAccessMapper;
import com.fooddelivery.restaurant.dataaccess.repository.RestaurantJpaRepository;
import com.fooddelivery.restaurant.domain.entity.Restaurant;
import com.fooddelivery.restaurant.domain.port.output.repository.RestaurantRepository;
import com.fooddelivery.restaurant.domain.valueobject.RestaurantId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RestaurantRepositoryImpl implements RestaurantRepository {

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;

    @Override
    public Restaurant save(Restaurant restaurant) {
        RestaurantJpaEntity jpaEntity = restaurantDataAccessMapper.restaurantToRestaurantJpaEntity(restaurant);
        RestaurantJpaEntity saved = restaurantJpaRepository.save(jpaEntity);
        return restaurantDataAccessMapper.restaurantJpaEntityToRestaurant(saved);
    }

    @Override
    public Optional<Restaurant> findById(RestaurantId restaurantId) {
        return restaurantJpaRepository.findById(restaurantId.getValue())
                .map(restaurantDataAccessMapper::restaurantJpaEntityToRestaurant);
    }

    @Override
    public Optional<Restaurant> findBySlug(String slug) {
        return restaurantJpaRepository.findBySlug(slug)
                .map(restaurantDataAccessMapper::restaurantJpaEntityToRestaurant);
    }

    @Override
    public List<Restaurant> findAll() {
        return restaurantDataAccessMapper.restaurantJpaEntitiesToRestaurants(restaurantJpaRepository.findAll());
    }

    @Override
    public List<Restaurant> findActiveNear(double latitude, double longitude, double radiusKm) {
        List<RestaurantJpaEntity> activeNear = restaurantJpaRepository.findActiveNear(latitude, longitude, radiusKm);
        return restaurantDataAccessMapper.restaurantJpaEntitiesToRestaurants(activeNear);
    }

    @Override
    public List<Restaurant> findByOwnerId(UUID ownerId) {
        return restaurantDataAccessMapper.restaurantJpaEntitiesToRestaurants(restaurantJpaRepository.findByOwnerId(ownerId));
    }

    @Override
    public void delete(RestaurantId restaurantId) {
        restaurantJpaRepository.deleteById(restaurantId.getValue());
    }
}
