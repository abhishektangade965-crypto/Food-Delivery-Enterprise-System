package com.fooddelivery.order.messaging.publisher;

import com.fooddelivery.order.domain.event.OrderCreatedEvent;
import com.fooddelivery.order.domain.port.output.message.publisher.OrderCreatedPaymentRequestMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestKafkaPublisher implements OrderCreatedPaymentRequestMessagePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "payment-request";

    @Override
    public void publish(OrderCreatedEvent event) {
        String key = event.getOrder().getId().getValue().toString();
        log.info("Publishing OrderCreatedPaymentRequest to topic {} for order: {}", TOPIC, key);

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", event.getOrder().getId().getValue().toString());
        payload.put("customerId", event.getOrder().getCustomerId().getValue().toString());
        payload.put("price", event.getOrder().getPrice().getAmount().toString());
        payload.put("paymentMethod", "WALLET"); // default for testing

        kafkaTemplate.send(TOPIC, key, payload);
    }
}
