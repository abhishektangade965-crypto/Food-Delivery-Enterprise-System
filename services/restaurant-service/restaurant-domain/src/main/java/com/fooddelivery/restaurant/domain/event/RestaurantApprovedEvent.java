package com.fooddelivery.restaurant.domain.event;

import com.fooddelivery.common.domain.event.DomainEvent;
import com.fooddelivery.restaurant.domain.entity.Restaurant;

import java.time.ZonedDateTime;
import java.util.UUID;

public class RestaurantApprovedEvent implements DomainEvent<Restaurant> {
    private final Restaurant restaurant;
    private final ZonedDateTime createdAt;
    private final String eventId;

    public RestaurantApprovedEvent(Restaurant restaurant, ZonedDateTime createdAt) {
        this.restaurant = restaurant;
        this.createdAt = createdAt;
        this.eventId = UUID.randomUUID().toString();
    }

    @Override
    public ZonedDateTime getOccurredOn() {
        return createdAt;
    }

    @Override
    public Restaurant getAggregate() {
        return restaurant;
    }

    @Override
    public String getEventId() {
        return eventId;
    }
}
