package com.fooddelivery.restaurant.domain.valueobject;

import com.fooddelivery.common.domain.valueobject.BaseId;
import java.util.UUID;

public final class MenuItemId extends BaseId<UUID> {

    public MenuItemId(UUID value) {
        super(value);
    }

    public static MenuItemId of(UUID value) {
        return new MenuItemId(value);
    }

    public static MenuItemId of(String value) {
        return new MenuItemId(UUID.fromString(value));
    }

    public static MenuItemId generate() {
        return new MenuItemId(UUID.randomUUID());
    }
}
