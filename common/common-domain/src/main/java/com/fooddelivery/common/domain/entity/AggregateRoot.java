package com.fooddelivery.common.domain.entity;

import com.fooddelivery.common.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for all aggregate roots.
 * Aggregate roots are the entry point for a group of related domain objects.
 * They maintain a list of domain events that occurred during the aggregate's lifecycle.
 * @param <ID> the type of the aggregate root's identifier
 */
public abstract class AggregateRoot<ID> extends BaseEntity<ID> {

    private final List<DomainEvent<?>> domainEvents = new ArrayList<>();

    protected AggregateRoot() {
        super();
    }

    protected AggregateRoot(ID id) {
        super(id);
    }

    /**
     * Registers a domain event to be published after the aggregate is persisted.
     */
    protected void registerEvent(DomainEvent<?> event) {
        domainEvents.add(event);
    }

    /**
     * Returns an unmodifiable view of the domain events.
     */
    public List<DomainEvent<?>> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Clears all registered domain events after they have been published.
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    /**
     * Returns true if there are any pending domain events.
     */
    public boolean hasDomainEvents() {
        return !domainEvents.isEmpty();
    }
}
