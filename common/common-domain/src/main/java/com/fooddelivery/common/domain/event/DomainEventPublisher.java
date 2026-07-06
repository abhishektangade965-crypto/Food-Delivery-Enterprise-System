package com.fooddelivery.common.domain.event;

/**
 * Generic interface for publishing domain events.
 * Each implementation handles a specific type of domain event.
 * @param <T> the type of domain event to publish
 */
@FunctionalInterface
public interface DomainEventPublisher<T extends DomainEvent<?>> {

    /**
     * Publishes the given domain event.
     * @param domainEvent the event to publish
     */
    void publish(T domainEvent);
}
