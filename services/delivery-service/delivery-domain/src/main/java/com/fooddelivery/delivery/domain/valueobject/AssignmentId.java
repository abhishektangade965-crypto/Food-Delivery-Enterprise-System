package com.fooddelivery.delivery.domain.valueobject;

import com.fooddelivery.common.domain.valueobject.BaseId;
import java.util.UUID;

public final class AssignmentId extends BaseId<UUID> {

    public AssignmentId(UUID value) {
        super(value);
    }

    public static AssignmentId of(UUID value) {
        return new AssignmentId(value);
    }

    public static AssignmentId of(String value) {
        return new AssignmentId(UUID.fromString(value));
    }

    public static AssignmentId generate() {
        return new AssignmentId(UUID.randomUUID());
    }
}
