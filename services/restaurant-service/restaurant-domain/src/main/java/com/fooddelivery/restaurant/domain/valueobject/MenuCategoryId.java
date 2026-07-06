package com.fooddelivery.restaurant.domain.valueobject;

import com.fooddelivery.common.domain.valueobject.BaseId;
import java.util.UUID;

public final class MenuCategoryId extends BaseId<UUID> {

    public MenuCategoryId(UUID value) {
        super(value);
    }

    public static MenuCategoryId of(UUID value) {
        return new MenuCategoryId(value);
    }

    public static MenuCategoryId of(String value) {
        return new MenuCategoryId(UUID.fromString(value));
    }

    public static MenuCategoryId generate() {
        return new MenuCategoryId(UUID.randomUUID());
    }
}
