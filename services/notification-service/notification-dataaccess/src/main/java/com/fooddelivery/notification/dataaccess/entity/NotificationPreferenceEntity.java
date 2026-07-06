package com.fooddelivery.notification.dataaccess.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification_preferences")
@Entity
public class NotificationPreferenceEntity {

    @Id
    private UUID userId;

    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean pushEnabled;
    private boolean whatsappEnabled;
}
