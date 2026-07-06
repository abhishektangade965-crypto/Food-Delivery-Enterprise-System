package com.fooddelivery.notification.application.dto;

import com.fooddelivery.notification.domain.valueobject.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    @NotNull(message = "Recipient ID is required")
    private UUID recipientId;

    private String recipientEmail;
    private String recipientPhone;
    private String recipientDeviceToken;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "Template name is required")
    private String templateName;

    private Map<String, String> templateVariables;
}
