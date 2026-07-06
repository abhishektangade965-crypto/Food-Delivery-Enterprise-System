package com.fooddelivery.notification.domain.port.output.repository;

import com.fooddelivery.notification.domain.entity.Notification;
import com.fooddelivery.notification.domain.entity.NotificationPreference;
import com.fooddelivery.notification.domain.valueobject.NotificationId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(NotificationId notificationId);
    List<Notification> findByRecipientId(UUID recipientId);
    List<Notification> findAll();

    NotificationPreference savePreference(NotificationPreference preference);
    Optional<NotificationPreference> findPreferenceByUserId(UUID userId);
}
