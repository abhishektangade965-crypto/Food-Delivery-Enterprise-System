package com.fooddelivery.delivery.domain.valueobject;

import com.fooddelivery.common.domain.valueobject.BaseId;
import java.util.UUID;

public final class DriverId extends BaseId<UUID> {

    public DriverId(UUID value) {
        super(value);
    }

    public static DriverId of(UUID value) {
        return new DriverId(value);
    }

    public static DriverId of(String value) {
        return new DriverId(UUID.fromString(value));
    }

    public static DriverId generate() {
        return new DriverId(UUID.randomUUID());
    }
}
