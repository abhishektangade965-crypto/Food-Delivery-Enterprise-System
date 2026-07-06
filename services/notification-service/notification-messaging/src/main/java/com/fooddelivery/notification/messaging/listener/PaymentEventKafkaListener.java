package com.fooddelivery.notification.messaging.listener;

import com.fooddelivery.notification.application.dto.SendNotificationRequest;
import com.fooddelivery.notification.application.service.NotificationApplicationService;
import com.fooddelivery.notification.domain.valueobject.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventKafkaListener {

    private final NotificationApplicationService notificationApplicationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String DLQ_TOPIC = "notification-dlq";

    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void receive(@Payload String paymentStatus, @Header(KafkaHeaders.RECEIVED_KEY) String orderId) {
        log.info("Received payment-event for orderId: {} status: {}", orderId, paymentStatus);
        try {
            String templateName;
            if ("FAILED".equalsIgnoreCase(paymentStatus)) {
                templateName = "payment-failed";
            } else if ("COMPLETED".equalsIgnoreCase(paymentStatus)) {
                templateName = "payment-completed";
            } else {
                log.info("Skipping notification for payment status: {}", paymentStatus);
                return;
            }

            SendNotificationRequest request = SendNotificationRequest.builder()
                    .recipientId(UUID.fromString(orderId))
                    .recipientEmail("customer@example.com") // Fallback recipient email
                    .type(NotificationType.EMAIL)
                    .templateName(templateName)
                    .templateVariables(Map.of(
                            "orderId", orderId,
                            "status", paymentStatus
                    ))
                    .build();
            notificationApplicationService.sendNotification(request);
        } catch (Exception e) {
            log.error("Error processing payment-event. Sending to DLQ. Error: {}", e.getMessage(), e);
            sendToDlq("payment-events", orderId, paymentStatus, e.getMessage());
        }
    }

    private void sendToDlq(String sourceTopic, String key, Object payload, String errorMessage) {
        try {
            Map<String, Object> dlqMessage = Map.of(
                    "sourceTopic", sourceTopic,
                    "key", key,
                    "payload", payload,
                    "errorMessage", errorMessage,
                    "timestamp", System.currentTimeMillis()
            );
            kafkaTemplate.send(DLQ_TOPIC, key, dlqMessage);
            log.info("Successfully sent failed message from {} to {}", sourceTopic, DLQ_TOPIC);
        } catch (Exception ex) {
            log.error("Failed to send message to DLQ: {}", ex.getMessage(), ex);
        }
    }
}
