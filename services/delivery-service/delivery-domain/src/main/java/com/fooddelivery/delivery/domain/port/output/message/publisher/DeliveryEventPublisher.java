package com.fooddelivery.delivery.domain.port.output.message.publisher;

import com.fooddelivery.delivery.domain.entity.DeliveryAssignment;

public interface DeliveryEventPublisher {
    void publish(DeliveryAssignment deliveryAssignment);
}
