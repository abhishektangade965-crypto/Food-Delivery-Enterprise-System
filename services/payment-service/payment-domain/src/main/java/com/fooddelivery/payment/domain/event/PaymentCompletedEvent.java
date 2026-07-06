package com.fooddelivery.payment.domain.event;

import com.fooddelivery.common.domain.event.DomainEvent;
import com.fooddelivery.payment.domain.entity.Payment;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class PaymentCompletedEvent implements DomainEvent<Payment> {
    private final Payment payment;
    private final ZonedDateTime createdAt;
    private final List<String> failureMessages;
    private final String eventId;

    public PaymentCompletedEvent(Payment payment, ZonedDateTime createdAt, List<String> failureMessages) {
        this.payment = payment;
        this.createdAt = createdAt;
        this.failureMessages = failureMessages;
        this.eventId = UUID.randomUUID().toString();
    }

    @Override
    public ZonedDateTime getOccurredOn() {
        return createdAt;
    }

    @Override
    public Payment getAggregate() {
        return payment;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }
}
