package com.fooddelivery.restaurant.application.service;

import com.fooddelivery.common.domain.exception.DomainNotFoundException;
import com.fooddelivery.restaurant.application.dto.*;
import com.fooddelivery.restaurant.application.mapper.RestaurantMapper;
import com.fooddelivery.restaurant.domain.entity.MenuCategory;
import com.fooddelivery.restaurant.domain.entity.Restaurant;
import com.fooddelivery.restaurant.domain.event.RestaurantApprovedEvent;
import com.fooddelivery.restaurant.domain.event.RestaurantMenuUpdatedEvent;
import com.fooddelivery.restaurant.domain.port.output.message.publisher.RestaurantApprovedMessagePublisher;
import com.fooddelivery.restaurant.domain.port.output.message.publisher.RestaurantMenuUpdatedMessagePublisher;
import com.fooddelivery.restaurant.domain.port.output.repository.RestaurantRepository;
import com.fooddelivery.restaurant.domain.service.RestaurantDomainService;
import com.fooddelivery.restaurant.domain.valueobject.RestaurantId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantApplicationServiceImpl implements RestaurantApplicationService {

    private final RestaurantDomainService restaurantDomainService;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantApprovedMessagePublisher restaurantApprovedMessagePublisher;
    private final RestaurantMenuUpdatedMessagePublisher restaurantMenuUpdatedMessagePublisher;
    private final RestaurantMapper restaurantMapper;

    @Override
    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
        Restaurant restaurant = restaurantMapper.createRequestToRestaurant(request);
        restaurantDomainService.initializeRestaurant(restaurant);
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Created restaurant with id: {}", saved.getId().getValue());
        return restaurantMapper.restaurantToRestaurantResponse(saved);
    }

    @Override
    @Transactional
    public RestaurantResponse updateRestaurant(UUID id, UpdateRestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(new RestaurantId(id))
                .orElseThrow(() -> new DomainNotFoundException("Restaurant not found with id: " + id));
        restaurantMapper.updateRestaurantFromRequest(request, restaurant);
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Updated restaurant details for id: {}", saved.getId().getValue());
        return restaurantMapper.restaurantToRestaurantResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(UUID id) {
        Restaurant restaurant = restaurantRepository.findById(new RestaurantId(id))
                .orElseThrow(() -> new DomainNotFoundException("Restaurant not found with id: " + id));
        return restaurantMapper.restaurantToRestaurantResponse(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantBySlug(String slug) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new DomainNotFoundException("Restaurant not found with slug: " + slug));
        return restaurantMapper.restaurantToRestaurantResponse(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantMapper.restaurantsToRestaurantResponses(restaurantRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> findActiveNear(double latitude, double longitude, double radiusKm) {
        List<Restaurant> activeRestaurants = restaurantRepository.findActiveNear(latitude, longitude, radiusKm);
        return restaurantMapper.restaurantsToRestaurantResponses(activeRestaurants);
    }

    @Override
    @Transactional
    public RestaurantResponse updateMenu(UUID id, UpdateMenuRequest request) {
        Restaurant restaurant = restaurantRepository.findById(new RestaurantId(id))
                .orElseThrow(() -> new DomainNotFoundException("Restaurant not found with id: " + id));
        List<MenuCategory> newCategories = restaurantMapper.categoryDtosToCategories(request.categories());
        RestaurantMenuUpdatedEvent menuUpdatedEvent = restaurantDomainService.updateMenu(restaurant, newCategories);
        Restaurant saved = restaurantRepository.save(restaurant);

        // Publish event
        try {
            restaurantMenuUpdatedMessagePublisher.publish(menuUpdatedEvent);
        } catch (Exception e) {
            log.error("Failed to publish MenuUpdated event for restaurant: " + id, e);
        }

        return restaurantMapper.restaurantToRestaurantResponse(saved);
    }

    @Override
    @Transactional
    public RestaurantResponse approveRestaurant(UUID id) {
        Restaurant restaurant = restaurantRepository.findById(new RestaurantId(id))
                .orElseThrow(() -> new DomainNotFoundException("Restaurant not found with id: " + id));
        RestaurantApprovedEvent approvedEvent = restaurantDomainService.approveRestaurant(restaurant);
        Restaurant saved = restaurantRepository.save(restaurant);

        // Publish event
        try {
            restaurantApprovedMessagePublisher.publish(approvedEvent);
        } catch (Exception e) {
            log.error("Failed to publish RestaurantApproved event for restaurant: " + id, e);
        }

        return restaurantMapper.restaurantToRestaurantResponse(saved);
    }

    @Override
    @Transactional
    public RestaurantResponse rejectRestaurant(UUID id) {
        Restaurant restaurant = restaurantRepository.findById(new RestaurantId(id))
                .orElseThrow(() -> new DomainNotFoundException("Restaurant not found with id: " + id));
        restaurantDomainService.rejectRestaurant(restaurant);
        Restaurant saved = restaurantRepository.save(restaurant);
        return restaurantMapper.restaurantToRestaurantResponse(saved);
    }

    @Override
    @Transactional
    public BigDecimal updateSurgeMultiplier(UUID id, int activeOrders, double weatherFactor) {
        Restaurant restaurant = restaurantRepository.findById(new RestaurantId(id))
                .orElseThrow(() -> new DomainNotFoundException("Restaurant not found with id: " + id));
        BigDecimal multiplier = restaurantDomainService.calculateSurgeMultiplier(restaurant, activeOrders, weatherFactor);
        restaurantRepository.save(restaurant);
        return multiplier;
    }
}
