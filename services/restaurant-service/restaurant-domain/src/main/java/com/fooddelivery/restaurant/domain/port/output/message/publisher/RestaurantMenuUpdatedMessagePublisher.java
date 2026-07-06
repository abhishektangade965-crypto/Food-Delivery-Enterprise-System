package com.fooddelivery.restaurant.domain.port.output.message.publisher;

import com.fooddelivery.restaurant.domain.event.RestaurantMenuUpdatedEvent;

public interface RestaurantMenuUpdatedMessagePublisher {
    void publish(RestaurantMenuUpdatedEvent event);
}
