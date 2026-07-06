package com.fooddelivery.restaurant.domain.port.output.message.publisher;

import com.fooddelivery.restaurant.domain.event.RestaurantApprovedEvent;

public interface RestaurantApprovedMessagePublisher {
    void publish(RestaurantApprovedEvent event);
}
