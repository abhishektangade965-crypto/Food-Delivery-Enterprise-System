package com.fooddelivery.user.domain.valueobject;

import com.fooddelivery.common.domain.valueobject.BaseId;
import java.util.UUID;

public class UserId extends BaseId<UUID> {
    public UserId(UUID value) {
        super(value);
    }
    public static UserId of(UUID value) { return new UserId(value); }
    public static UserId generate() { return new UserId(UUID.randomUUID()); }
}
