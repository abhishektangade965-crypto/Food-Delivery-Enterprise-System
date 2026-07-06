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
public class UserEventKafkaListener {

    private final NotificationApplicationService notificationApplicationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String DLQ_TOPIC = "notification-dlq";

    @KafkaListener(topics = "user-events", groupId = "notification-service-group")
    public void receive(@Payload String email, @Header(KafkaHeaders.RECEIVED_KEY) String userId) {
        log.info("Received user-event for userId: {} with email: {}", userId, email);
        try {
            SendNotificationRequest request = SendNotificationRequest.builder()
                    .recipientId(UUID.fromString(userId))
                    .recipientEmail(email)
                    .type(NotificationType.EMAIL)
                    .templateName("user-created")
                    .templateVariables(Map.of("email", email, "userId", userId))
                    .build();
            notificationApplicationService.sendNotification(request);
        } catch (Exception e) {
            log.error("Error processing user-event. Sending to DLQ. Error: {}", e.getMessage(), e);
            sendToDlq("user-events", userId, email, e.getMessage());
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
