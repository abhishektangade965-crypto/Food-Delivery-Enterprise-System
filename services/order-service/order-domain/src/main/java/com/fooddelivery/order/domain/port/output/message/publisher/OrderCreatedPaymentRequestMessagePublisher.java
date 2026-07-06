package com.fooddelivery.order.domain.port.output.message.publisher;

import com.fooddelivery.order.domain.event.OrderCreatedEvent;

public interface OrderCreatedPaymentRequestMessagePublisher {
    void publish(OrderCreatedEvent event);
}
