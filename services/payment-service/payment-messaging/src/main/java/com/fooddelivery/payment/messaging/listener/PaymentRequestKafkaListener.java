package com.fooddelivery.payment.messaging.listener;

import com.fooddelivery.payment.application.dto.PaymentRequest;
import com.fooddelivery.payment.application.service.PaymentApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestKafkaListener {

    private final PaymentApplicationService paymentApplicationService;

    @KafkaListener(topics = "payment-request", groupId = "payment-service-group")
    public void receive(@Payload PaymentRequest request) {
        log.info("Received payment-request in payment-service for order id: {}", request.orderId());
        try {
            paymentApplicationService.processPayment(request);
        } catch (Exception e) {
            log.error("Error processing payment request via Kafka listener", e);
        }
    }
}
