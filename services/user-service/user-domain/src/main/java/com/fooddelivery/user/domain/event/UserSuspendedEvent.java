package com.fooddelivery.user.domain.event;

import com.fooddelivery.common.domain.event.DomainEvent;
import com.fooddelivery.user.domain.entity.User;

import java.time.ZonedDateTime;
import java.util.UUID;

public class UserSuspendedEvent implements DomainEvent<User> {
    private final User user;
    private final ZonedDateTime suspendedAt;
    private final String eventId;

    public UserSuspendedEvent(User user, ZonedDateTime suspendedAt) {
        this.user = user;
        this.suspendedAt = suspendedAt;
        this.eventId = UUID.randomUUID().toString();
    }

    @Override
    public ZonedDateTime getOccurredOn() {
        return suspendedAt;
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
