package com.fooddelivery.notification.dataaccess.entity;

import com.fooddelivery.notification.dataaccess.converter.MapConverter;
import com.fooddelivery.notification.domain.valueobject.NotificationStatus;
import com.fooddelivery.notification.domain.valueobject.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
@Entity
public class NotificationEntity {

    @Id
    private UUID id;

    private UUID recipientId;
    private String recipientEmail;
    private String recipientPhone;
    private String recipientDeviceToken;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String title;
    private String body;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private ZonedDateTime sentAt;
    private int retryCount;
    private String failureReason;
    private String templateName;

    @Convert(converter = MapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, String> templateVariables;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
