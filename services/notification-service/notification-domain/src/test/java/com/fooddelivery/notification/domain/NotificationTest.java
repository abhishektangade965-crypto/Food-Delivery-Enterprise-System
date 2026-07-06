package com.fooddelivery.notification.domain;

import com.fooddelivery.notification.domain.entity.Notification;
import com.fooddelivery.notification.domain.valueobject.NotificationStatus;
import com.fooddelivery.notification.domain.valueobject.NotificationType;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationTest {

    @Test
    public void testNotificationInitialization() {
        Notification notification = Notification.builder()
                .recipientId(UUID.randomUUID())
                .recipientEmail("user@example.com")
                .type(NotificationType.EMAIL)
                .templateName("user-created")
                .templateVariables(Map.of("email", "user@example.com"))
                .build();

        notification.initializeNotification();

        assertNotNull(notification.getId());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        assertEquals(0, notification.getRetryCount());
        assertNotNull(notification.getCreatedAt());
    }

    @Test
    public void testNotificationRendering() {
        Notification notification = Notification.builder()
                .recipientId(UUID.randomUUID())
                .type(NotificationType.EMAIL)
                .templateName("user-created")
                .build();
        notification.initializeNotification();

        notification.renderContent("Welcome title", "Hello, user body!");

        assertEquals("Welcome title", notification.getTitle());
        assertEquals("Hello, user body!", notification.getBody());
    }

    @Test
    public void testNotificationRetryFailureAndSuccess() {
        Notification notification = Notification.builder()
                .recipientId(UUID.randomUUID())
                .type(NotificationType.SMS)
                .templateName("order-placed")
                .build();
        notification.initializeNotification();

        // Simulate first failure (not reached max retry count of 3)
        notification.markFailed("Gateway Timeout", 3);
        assertEquals(1, notification.getRetryCount());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());

        // Simulate second failure
        notification.markFailed("Gateway Timeout", 3);
        assertEquals(2, notification.getRetryCount());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());

        // Simulate third failure (reaches max retry count of 3)
        notification.markFailed("Gateway Timeout", 3);
        assertEquals(3, notification.getRetryCount());
        assertEquals(NotificationStatus.FAILED, notification.getStatus());
        assertEquals("Gateway Timeout", notification.getFailureReason());

        // Reset and try success
        Notification successNotification = Notification.builder()
                .recipientId(UUID.randomUUID())
                .type(NotificationType.EMAIL)
                .templateName("order-placed")
                .build();
        successNotification.initializeNotification();
        
        ZonedDateTime sentTime = ZonedDateTime.now();
        successNotification.markSent(sentTime);
        assertEquals(NotificationStatus.SENT, successNotification.getStatus());
        assertEquals(sentTime, successNotification.getSentAt());
        assertNull(successNotification.getFailureReason());
    }
}
