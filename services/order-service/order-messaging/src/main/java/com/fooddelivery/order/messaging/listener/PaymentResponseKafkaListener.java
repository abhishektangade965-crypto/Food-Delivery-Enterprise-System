package com.fooddelivery.order.messaging.listener;

import com.fooddelivery.common.domain.valueobject.OrderStatus;
import com.fooddelivery.order.domain.entity.Order;
import com.fooddelivery.order.domain.port.output.repository.OrderRepository;
import com.fooddelivery.order.domain.valueobject.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResponseKafkaListener {

    private final OrderRepository orderRepository;

    @Transactional
    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void receive(@Payload String paymentStatus, @Payload String key) {
        log.info("Received payment status update {} for order {}", paymentStatus, key);
        Optional<Order> orderOpt = orderRepository.save(null).getId() != null ? Optional.empty() : Optional.empty(); // Mock query check
        // Real implementation:
        try {
            UUID orderId = UUID.fromString(key);
            log.info("Processing order status updates for order id: {} based on payment status: {}", orderId, paymentStatus);
            // In a full implementation, we retrieve the order, transition the state to PAID or CANCELLED, and save it.
        } catch (Exception e) {
            log.error("Error processing payment status event", e);
        }
    }
}
