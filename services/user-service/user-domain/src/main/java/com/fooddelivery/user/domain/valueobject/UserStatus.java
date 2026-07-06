package com.fooddelivery.user.domain.valueobject;

public enum UserStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    DELETED;

    public boolean isAccessible() {
        return this == ACTIVE;
    }

    public boolean canTransitionTo(UserStatus newStatus) {
        return switch (this) {
            case PENDING_VERIFICATION -> newStatus == ACTIVE || newStatus == DELETED;
            case ACTIVE -> newStatus == INACTIVE || newStatus == SUSPENDED || newStatus == DELETED;
            case INACTIVE -> newStatus == ACTIVE || newStatus == DELETED;
            case SUSPENDED -> newStatus == ACTIVE || newStatus == DELETED;
            case DELETED -> false;
        };
    }
}
