package com.fooddelivery.payment.messaging.publisher;

import com.fooddelivery.payment.domain.event.PaymentCompletedEvent;
import com.fooddelivery.payment.domain.event.PaymentFailedEvent;
import com.fooddelivery.payment.domain.event.PaymentRefundedEvent;
import com.fooddelivery.payment.domain.port.output.message.publisher.PaymentResponseMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResponseKafkaPublisher implements PaymentResponseMessagePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "payment-events";

    @Override
    public void publish(PaymentCompletedEvent event) {
        String key = event.getPayment().getOrderId().getValue().toString();
        log.info("Publishing PaymentCompletedEvent to topic {} with key {}", TOPIC, key);
        kafkaTemplate.send(TOPIC, key, event.getPayment().getPaymentStatus().name());
    }

    @Override
    public void publish(PaymentFailedEvent event) {
        String key = event.getPayment().getOrderId().getValue().toString();
        log.info("Publishing PaymentFailedEvent to topic {} with key {}", TOPIC, key);
        kafkaTemplate.send(TOPIC, key, event.getPayment().getPaymentStatus().name());
    }

    @Override
    public void publish(PaymentRefundedEvent event) {
        String key = event.getPayment().getOrderId().getValue().toString();
        log.info("Publishing PaymentRefundedEvent to topic {} with key {}", TOPIC, key);
        kafkaTemplate.send(TOPIC, key, event.getPayment().getPaymentStatus().name());
    }
}
