package com.fooddelivery.restaurant.domain.valueobject;

import com.fooddelivery.common.domain.valueobject.BaseId;
import java.util.UUID;

public final class InventoryItemId extends BaseId<UUID> {

    public InventoryItemId(UUID value) {
        super(value);
    }

    public static InventoryItemId of(UUID value) {
        return new InventoryItemId(value);
    }

    public static InventoryItemId of(String value) {
        return new InventoryItemId(UUID.fromString(value));
    }

    public static InventoryItemId generate() {
        return new InventoryItemId(UUID.randomUUID());
    }
}
