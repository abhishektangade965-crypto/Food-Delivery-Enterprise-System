package com.fooddelivery.common.domain.valueobject;

import java.util.UUID;

/**
 * Strongly-typed identifier for Product aggregate.
 */
public final class ProductId extends BaseId<UUID> {

    public ProductId(UUID value) {
        super(value);
    }

    public static ProductId of(UUID value) {
        return new ProductId(value);
    }

    public static ProductId of(String value) {
        return new ProductId(UUID.fromString(value));
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID());
    }
}
