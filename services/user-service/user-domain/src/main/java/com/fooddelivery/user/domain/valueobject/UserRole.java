package com.fooddelivery.user.domain.valueobject;

public enum UserRole {
    CUSTOMER,
    RESTAURANT_OWNER,
    DELIVERY_DRIVER,
    ADMIN,
    SUPER_ADMIN,
    SUPPORT_AGENT;

    public boolean isAdminRole() {
        return this == ADMIN || this == SUPER_ADMIN || this == SUPPORT_AGENT;
    }

    public boolean isStaffRole() {
        return this == RESTAURANT_OWNER || this == DELIVERY_DRIVER;
    }
}
