package com.fooddelivery.user.domain.event;

import com.fooddelivery.common.domain.event.DomainEvent;
import com.fooddelivery.user.domain.entity.User;

import java.time.ZonedDateTime;
import java.util.UUID;

public class UserActivatedEvent implements DomainEvent<User> {
    private final User user;
    private final ZonedDateTime activatedAt;
    private final String eventId;

    public UserActivatedEvent(User user, ZonedDateTime activatedAt) {
        this.user = user;
        this.activatedAt = activatedAt;
        this.eventId = UUID.randomUUID().toString();
    }

    @Override
    public ZonedDateTime getOccurredOn() {
        return activatedAt;
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
