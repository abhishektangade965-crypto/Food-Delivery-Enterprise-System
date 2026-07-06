package com.fooddelivery.notification.application.dto;

import com.fooddelivery.notification.domain.valueobject.NotificationStatus;
import com.fooddelivery.notification.domain.valueobject.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID notificationId;
    private UUID recipientId;
    private String recipientEmail;
    private String recipientPhone;
    private String recipientDeviceToken;
    private NotificationType type;
    private String title;
    private String body;
    private NotificationStatus status;
    private ZonedDateTime sentAt;
    private int retryCount;
    private String failureReason;
    private String templateName;
    private Map<String, String> templateVariables;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
