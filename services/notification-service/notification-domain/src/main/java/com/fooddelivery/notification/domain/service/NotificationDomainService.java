package com.fooddelivery.notification.domain.service;

import com.fooddelivery.notification.domain.entity.Notification;

public interface NotificationDomainService {
    void validateAndRender(Notification notification, String templateTitle, String templateBody);
    boolean canRetry(Notification notification, int maxRetryAttempts);
}
