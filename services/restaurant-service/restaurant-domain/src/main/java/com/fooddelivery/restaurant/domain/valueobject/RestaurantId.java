package com.fooddelivery.restaurant.domain.valueobject;

import com.fooddelivery.common.domain.valueobject.BaseId;
import java.util.UUID;

public final class RestaurantId extends BaseId<UUID> {

    public RestaurantId(UUID value) {
        super(value);
    }

    public static RestaurantId of(UUID value) {
        return new RestaurantId(value);
    }

    public static RestaurantId of(String value) {
        return new RestaurantId(UUID.fromString(value));
    }

    public static RestaurantId generate() {
        return new RestaurantId(UUID.randomUUID());
    }
}
