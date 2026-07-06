package com.fooddelivery.notification.messaging.listener;

import com.fooddelivery.notification.application.dto.SendNotificationRequest;
import com.fooddelivery.notification.application.service.NotificationApplicationService;
import com.fooddelivery.notification.domain.valueobject.NotificationType;
import com.fooddelivery.notification.messaging.model.OrderEventKafkaModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventKafkaListener {

    private final NotificationApplicationService notificationApplicationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String DLQ_TOPIC = "notification-dlq";

    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void receive(@Payload OrderEventKafkaModel model, @Header(KafkaHeaders.RECEIVED_KEY) String orderId) {
        log.info("Received order-event for orderId: {} status: {}", orderId, model.getStatus());
        try {
            SendNotificationRequest request = SendNotificationRequest.builder()
                    .recipientId(model.getCustomerId())
                    .recipientEmail(model.getCustomerEmail())
                    .recipientPhone(model.getCustomerPhone())
                    .type(model.getCustomerEmail() != null ? NotificationType.EMAIL : NotificationType.SMS)
                    .templateName("order-placed")
                    .templateVariables(Map.of(
                            "orderId", model.getOrderId().toString(),
                            "customerId", model.getCustomerId().toString(),
                            "amount", model.getAmount().toString(),
                            "status", model.getStatus()
                    ))
                    .build();
            notificationApplicationService.sendNotification(request);
        } catch (Exception e) {
            log.error("Error processing order-event. Sending to DLQ. Error: {}", e.getMessage(), e);
            sendToDlq("order-events", orderId, model, e.getMessage());
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
