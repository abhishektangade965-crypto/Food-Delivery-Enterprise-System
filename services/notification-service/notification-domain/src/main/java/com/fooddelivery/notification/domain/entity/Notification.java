package com.fooddelivery.notification.domain.entity;

import com.fooddelivery.common.domain.entity.AggregateRoot;
import com.fooddelivery.notification.domain.valueobject.NotificationId;
import com.fooddelivery.notification.domain.valueobject.NotificationStatus;
import com.fooddelivery.notification.domain.valueobject.NotificationType;
import lombok.Getter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
public class Notification extends AggregateRoot<NotificationId> {

    private final UUID recipientId;
    private final String recipientEmail;
    private final String recipientPhone;
    private final String recipientDeviceToken;
    private final NotificationType type;
    private String title;
    private String body;
    private NotificationStatus status;
    private ZonedDateTime sentAt;
    private int retryCount;
    private String failureReason;
    private final String templateName;
    private Map<String, String> templateVariables;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    private Notification(Builder builder) {
        super.setId(builder.notificationId);
        this.recipientId = builder.recipientId;
        this.recipientEmail = builder.recipientEmail;
        this.recipientPhone = builder.recipientPhone;
        this.recipientDeviceToken = builder.recipientDeviceToken;
        this.type = builder.type;
        this.title = builder.title;
        this.body = builder.body;
        this.status = builder.status;
        this.sentAt = builder.sentAt;
        this.retryCount = builder.retryCount;
        this.failureReason = builder.failureReason;
        this.templateName = builder.templateName;
        this.templateVariables = builder.templateVariables;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public void initializeNotification() {
        if (getId() == null) {
            setId(new NotificationId(UUID.randomUUID()));
        }
        this.status = NotificationStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public void renderContent(String renderedTitle, String renderedBody) {
        this.title = renderedTitle;
        this.body = renderedBody;
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public void markSent(ZonedDateTime sentAt) {
        this.status = NotificationStatus.SENT;
        this.sentAt = sentAt;
        this.failureReason = null;
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public void markFailed(String reason, int maxRetries) {
        this.retryCount++;
        this.failureReason = reason;
        if (this.retryCount >= maxRetries) {
            this.status = NotificationStatus.FAILED;
        } else {
            this.status = NotificationStatus.PENDING;
        }
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public void setTemplateVariables(Map<String, String> templateVariables) {
        this.templateVariables = templateVariables;
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private NotificationId notificationId;
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

        private Builder() {}

        public Builder notificationId(NotificationId val) { notificationId = val; return this; }
        public Builder recipientId(UUID val) { recipientId = val; return this; }
        public Builder recipientEmail(String val) { recipientEmail = val; return this; }
        public Builder recipientPhone(String val) { recipientPhone = val; return this; }
        public Builder recipientDeviceToken(String val) { recipientDeviceToken = val; return this; }
        public Builder type(NotificationType val) { type = val; return this; }
        public Builder title(String val) { title = val; return this; }
        public Builder body(String val) { body = val; return this; }
        public Builder status(NotificationStatus val) { status = val; return this; }
        public Builder sentAt(ZonedDateTime val) { sentAt = val; return this; }
        public Builder retryCount(int val) { retryCount = val; return this; }
        public Builder failureReason(String val) { failureReason = val; return this; }
        public Builder templateName(String val) { templateName = val; return this; }
        public Builder templateVariables(Map<String, String> val) { templateVariables = val; return this; }
        public Builder createdAt(ZonedDateTime val) { createdAt = val; return this; }
        public Builder updatedAt(ZonedDateTime val) { updatedAt = val; return this; }

        public Notification build() {
            return new Notification(this);
        }
    }
}
