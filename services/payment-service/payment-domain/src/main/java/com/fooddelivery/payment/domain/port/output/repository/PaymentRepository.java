package com.fooddelivery.payment.domain.port.output.repository;

import com.fooddelivery.common.domain.valueobject.OrderId;
import com.fooddelivery.payment.domain.entity.Payment;
import com.fooddelivery.payment.domain.valueobject.PaymentId;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(PaymentId paymentId);
    Optional<Payment> findByOrderId(OrderId orderId);
}
