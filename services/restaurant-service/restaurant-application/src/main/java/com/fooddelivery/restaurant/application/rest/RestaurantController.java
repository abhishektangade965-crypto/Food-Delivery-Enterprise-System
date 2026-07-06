package com.fooddelivery.restaurant.application.rest;

import com.fooddelivery.restaurant.application.dto.*;
import com.fooddelivery.restaurant.application.service.RestaurantApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantApplicationService restaurantApplicationService;

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(@RequestBody @Valid CreateRestaurantRequest request) {
        log.info("REST request to create restaurant: {}", request.name());
        RestaurantResponse response = restaurantApplicationService.createRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateRestaurantRequest request) {
        log.info("REST request to update restaurant: {}", id);
        RestaurantResponse response = restaurantApplicationService.updateRestaurant(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable UUID id) {
        log.info("REST request to get restaurant by id: {}", id);
        RestaurantResponse response = restaurantApplicationService.getRestaurantById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<RestaurantResponse> getRestaurantBySlug(@PathVariable String slug) {
        log.info("REST request to get restaurant by slug: {}", slug);
        RestaurantResponse response = restaurantApplicationService.getRestaurantBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getRestaurants(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false, defaultValue = "5.0") Double radiusKm) {
        if (lat != null && lon != null) {
            log.info("REST request to find active restaurants near lat: {}, lon: {}, radius: {} km", lat, lon, radiusKm);
            List<RestaurantResponse> responses = restaurantApplicationService.findActiveNear(lat, lon, radiusKm);
            return ResponseEntity.ok(responses);
        } else {
            log.info("REST request to get all restaurants");
            List<RestaurantResponse> responses = restaurantApplicationService.getAllRestaurants();
            return ResponseEntity.ok(responses);
        }
    }

    @PutMapping("/{id}/menu")
    public ResponseEntity<RestaurantResponse> updateMenu(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateMenuRequest request) {
        log.info("REST request to update menu for restaurant: {}", id);
        RestaurantResponse response = restaurantApplicationService.updateMenu(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<RestaurantResponse> approveRestaurant(@PathVariable UUID id) {
        log.info("REST request to approve restaurant: {}", id);
        RestaurantResponse response = restaurantApplicationService.approveRestaurant(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RestaurantResponse> rejectRestaurant(@PathVariable UUID id) {
        log.info("REST request to reject restaurant: {}", id);
        RestaurantResponse response = restaurantApplicationService.rejectRestaurant(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/surge")
    public ResponseEntity<BigDecimal> updateSurge(
            @PathVariable UUID id,
            @RequestParam int activeOrders,
            @RequestParam double weatherFactor) {
        log.info("REST request to update surge for restaurant: {}, activeOrders: {}, weatherFactor: {}", id, activeOrders, weatherFactor);
        BigDecimal multiplier = restaurantApplicationService.updateSurgeMultiplier(id, activeOrders, weatherFactor);
        return ResponseEntity.ok(multiplier);
    }
}
