package com.fooddelivery.common.infrastructure.outbox;

/**
 * Represents the processing status of an outbox message.
 */
public enum OutboxStatus {

    /** Message has been saved to outbox but not yet published. */
    STARTED,

    /** Message has been successfully published to the message broker. */
    COMPLETED,

    /** Message processing has permanently failed after all retries. */
    FAILED
}
