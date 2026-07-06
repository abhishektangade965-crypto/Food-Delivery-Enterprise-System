package com.fooddelivery.order.domain.event;

import com.fooddelivery.common.domain.event.DomainEvent;
import com.fooddelivery.order.domain.entity.Order;

import java.time.ZonedDateTime;
import java.util.UUID;

public class OrderCreatedEvent implements DomainEvent<Order> {
    private final Order order;
    private final ZonedDateTime createdAt;
    private final String eventId;

    public OrderCreatedEvent(Order order, ZonedDateTime createdAt) {
        this.order = order;
        this.createdAt = createdAt;
        this.eventId = UUID.randomUUID().toString();
    }

    @Override
    public ZonedDateTime getOccurredOn() {
        return createdAt;
    }

    @Override
    public Order getAggregate() {
        return order;
    }

    @Override
    public String getEventId() {
        return eventId;
    }
}
