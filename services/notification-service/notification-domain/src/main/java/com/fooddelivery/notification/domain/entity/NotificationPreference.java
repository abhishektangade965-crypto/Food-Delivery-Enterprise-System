package com.fooddelivery.notification.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class NotificationPreference {
    private final UUID userId;
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean pushEnabled;
    private boolean whatsappEnabled;

    public static NotificationPreference getDefault(UUID userId) {
        return NotificationPreference.builder()
                .userId(userId)
                .emailEnabled(true)
                .smsEnabled(true)
                .pushEnabled(true)
                .whatsappEnabled(true)
                .build();
    }
}
