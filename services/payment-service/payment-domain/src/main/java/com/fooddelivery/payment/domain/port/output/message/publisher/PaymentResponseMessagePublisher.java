package com.fooddelivery.payment.domain.port.output.message.publisher;

import com.fooddelivery.payment.domain.event.PaymentCompletedEvent;
import com.fooddelivery.payment.domain.event.PaymentFailedEvent;
import com.fooddelivery.payment.domain.event.PaymentRefundedEvent;

public interface PaymentResponseMessagePublisher {
    void publish(PaymentCompletedEvent event);
    void publish(PaymentFailedEvent event);
    void publish(PaymentRefundedEvent event);
}
