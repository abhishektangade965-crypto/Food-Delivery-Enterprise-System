package com.fooddelivery.notification.messaging.listener;

import com.fooddelivery.notification.application.dto.SendNotificationRequest;
import com.fooddelivery.notification.application.service.NotificationApplicationService;
import com.fooddelivery.notification.domain.valueobject.NotificationType;
import com.fooddelivery.notification.messaging.model.DeliveryEventKafkaModel;
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
public class DeliveryEventKafkaListener {

    private final NotificationApplicationService notificationApplicationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String DLQ_TOPIC = "notification-dlq";

    @KafkaListener(topics = "delivery-events", groupId = "notification-service-group")
    public void receive(@Payload DeliveryEventKafkaModel model, @Header(KafkaHeaders.RECEIVED_KEY) String deliveryId) {
        log.info("Received delivery-event for deliveryId: {} status: {}", deliveryId, model.getStatus());
        try {
            String templateName;
            if ("ASSIGNED".equalsIgnoreCase(model.getStatus())) {
                templateName = "driver-assigned";
            } else if ("DELIVERED".equalsIgnoreCase(model.getStatus())) {
                templateName = "order-delivered";
            } else {
                log.info("Skipping notification for delivery status: {}", model.getStatus());
                return;
            }

            SendNotificationRequest request = SendNotificationRequest.builder()
                    .recipientId(model.getCustomerId())
                    .recipientPhone(model.getCustomerPhone())
                    .type(NotificationType.SMS) // Defaulting SMS/Push for delivery updates
                    .templateName(templateName)
                    .templateVariables(Map.of(
                            "deliveryId", model.getDeliveryId().toString(),
                            "orderId", model.getOrderId().toString(),
                            "driverName", model.getDriverName() != null ? model.getDriverName() : "driver",
                            "status", model.getStatus()
                    ))
                    .build();
            notificationApplicationService.sendNotification(request);
        } catch (Exception e) {
            log.error("Error processing delivery-event. Sending to DLQ. Error: {}", e.getMessage(), e);
            sendToDlq("delivery-events", deliveryId, model, e.getMessage());
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
