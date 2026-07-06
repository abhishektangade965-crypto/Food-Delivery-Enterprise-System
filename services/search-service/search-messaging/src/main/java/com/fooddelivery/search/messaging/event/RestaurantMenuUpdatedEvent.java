package com.fooddelivery.search.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantMenuUpdatedEvent {
    private UUID restaurantId;
    private List<String> cuisineTypes;
    private List<String> tags;
}
