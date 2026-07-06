package com.fooddelivery.order.domain.port.output.message.publisher;

import com.fooddelivery.order.domain.event.OrderCancelledEvent;

public interface OrderCancelledPaymentRequestMessagePublisher {
    void publish(OrderCancelledEvent event);
}
