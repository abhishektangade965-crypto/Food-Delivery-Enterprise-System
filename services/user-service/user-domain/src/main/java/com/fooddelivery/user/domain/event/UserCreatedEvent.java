package com.fooddelivery.user.domain.event;

import com.fooddelivery.common.domain.event.DomainEvent;
import com.fooddelivery.user.domain.entity.User;

import java.time.ZonedDateTime;
import java.util.UUID;

public class UserCreatedEvent implements DomainEvent<User> {
    private final User user;
    private final ZonedDateTime createdAt;
    private final String eventId;

    public UserCreatedEvent(User user, ZonedDateTime createdAt) {
        this.user = user;
        this.createdAt = createdAt;
        this.eventId = UUID.randomUUID().toString();
    }

    @Override
    public ZonedDateTime getOccurredOn() {
        return createdAt;
    }

    @Override
    public User getAggregate() {
        return user;
    }

    @Override
    public String getEventId() {
        return eventId;
    }
}
