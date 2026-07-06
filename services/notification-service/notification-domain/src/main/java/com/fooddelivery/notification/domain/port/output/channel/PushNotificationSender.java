package com.fooddelivery.notification.domain.port.output.channel;

import com.fooddelivery.notification.domain.entity.Notification;

public interface PushNotificationSender {
    void send(Notification notification);
}
