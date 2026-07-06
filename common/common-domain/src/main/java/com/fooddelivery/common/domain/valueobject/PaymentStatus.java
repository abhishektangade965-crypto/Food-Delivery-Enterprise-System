package com.fooddelivery.common.domain.valueobject;

/**
 * Represents the lifecycle status of a Payment.
 */
public enum PaymentStatus {

    /** Payment has been initiated but not yet processed. */
    INITIATED,

    /** Payment has been successfully completed. */
    COMPLETED,

    /** Payment has been cancelled before processing. */
    CANCELLED,

    /** Payment processing has failed. */
    FAILED,

    /** Payment has been refunded to the customer. */
    REFUNDED,

    /** Payment has been created and is pending validation */
    PENDING,

    /** Payment refund is in progress */
    REFUND_INITIATED;

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED || this == FAILED || this == REFUNDED;
    }

    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    public boolean requiresAction() {
        return this == INITIATED;
    }
}
