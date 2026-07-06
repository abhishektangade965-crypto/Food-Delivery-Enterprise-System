package com.fooddelivery.common.domain.valueobject;

/**
 * Represents the lifecycle status of an Order.
 */
public enum OrderStatus {

    /** Order has been placed but not yet paid. */
    PENDING,

    /** Payment has been successfully completed. */
    PAID,

    /** Restaurant has approved and accepted the order. */
    APPROVED,

    /** Cancellation has been requested and is in progress. */
    CANCELLING,

    /** Order has been fully cancelled. */
    CANCELLED,

    /** Restaurant is currently preparing the order. */
    PREPARING,

    /** Order is ready for pickup by delivery partner. */
    READY_FOR_PICKUP,

    /** Delivery partner has picked up the order. */
    PICKED_UP,

    /** Order is currently being delivered. */
    DELIVERING,

    /** Order has been successfully delivered to customer. */
    DELIVERED;

    public boolean isTerminal() {
        return this == CANCELLED || this == DELIVERED;
    }

    public boolean isCancellable() {
        return this == PENDING || this == PAID || this == APPROVED;
    }

    public boolean isActive() {
        return !isTerminal();
    }
}
