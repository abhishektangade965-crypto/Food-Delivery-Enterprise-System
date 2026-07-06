package com.fooddelivery.common.domain.valueobject;

import java.util.UUID;

/**
 * Strongly-typed identifier for Order aggregate.
 */
public final class OrderId extends BaseId<UUID> {

    public OrderId(UUID value) {
        super(value);
    }

    public static OrderId of(UUID value) {
        return new OrderId(value);
    }

    public static OrderId of(String value) {
        return new OrderId(UUID.fromString(value));
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID());
    }
}
