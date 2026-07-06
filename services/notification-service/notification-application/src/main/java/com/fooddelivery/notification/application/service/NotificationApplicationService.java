package com.fooddelivery.notification.application.service;

import com.fooddelivery.notification.application.dto.NotificationPreferenceRequest;
import com.fooddelivery.notification.application.dto.NotificationResponse;
import com.fooddelivery.notification.application.dto.SendNotificationRequest;
import com.fooddelivery.notification.domain.entity.NotificationPreference;

import java.util.List;
import java.util.UUID;

public interface NotificationApplicationService {
    NotificationResponse sendNotification(SendNotificationRequest request);
    List<NotificationResponse> getNotificationLogs(UUID recipientId);
    List<NotificationResponse> getAllNotificationLogs();
    NotificationPreference updatePreferences(UUID userId, NotificationPreferenceRequest request);
    NotificationPreference getPreferences(UUID userId);
}
