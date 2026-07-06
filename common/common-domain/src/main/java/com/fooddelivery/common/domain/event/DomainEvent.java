package com.fooddelivery.common.domain.event;

import java.time.ZonedDateTime;

/**
 * Marker interface for all domain events.
 * Domain events represent something that happened in the domain.
 * @param <T> the type of the aggregate that raised this event
 */
public interface DomainEvent<T> {

    /**
     * Returns the time at which this event occurred.
     */
    ZonedDateTime getOccurredOn();

    /**
     * Returns the aggregate that raised this event.
     */
    T getAggregate();

    /**
     * Returns the unique event ID for idempotency checking.
     */
    String getEventId();

    /**
     * Returns the event type name for routing and deserialization.
     */
    default String getEventType() {
        return getClass().getSimpleName();
    }
}
