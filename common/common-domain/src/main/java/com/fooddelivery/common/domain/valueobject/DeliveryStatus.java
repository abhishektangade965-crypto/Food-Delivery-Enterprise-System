package com.fooddelivery.common.domain.valueobject;

/**
 * Represents the lifecycle status of a Delivery.
 */
public enum DeliveryStatus {

    /** Delivery request is pending assignment. */
    PENDING,

    /** Delivery has been assigned to a delivery partner. */
    ASSIGNED,

    /** Delivery partner has picked up the order from restaurant. */
    PICKED_UP,

    /** Order is currently being delivered to the customer. */
    DELIVERING,

    /** Order has been successfully delivered. */
    DELIVERED,

    /** Delivery has failed for some reason. */
    FAILED;

    public boolean isTerminal() {
        return this == DELIVERED || this == FAILED;
    }

    public boolean isActive() {
        return !isTerminal() && this != PENDING;
    }

    public boolean canBeAssigned() {
        return this == PENDING;
    }
}
