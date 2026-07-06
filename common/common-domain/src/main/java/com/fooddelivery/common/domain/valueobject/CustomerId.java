package com.fooddelivery.common.domain.valueobject;

import java.util.UUID;

/**
 * Strongly-typed identifier for Customer aggregate.
 */
public final class CustomerId extends BaseId<UUID> {

    public CustomerId(UUID value) {
        super(value);
    }

    public static CustomerId of(UUID value) {
        return new CustomerId(value);
    }

    public static CustomerId of(String value) {
        return new CustomerId(UUID.fromString(value));
    }

    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }
}
