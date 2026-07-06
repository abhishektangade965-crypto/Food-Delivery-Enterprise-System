package com.fooddelivery.notification.domain.port.output.channel;

import com.fooddelivery.notification.domain.entity.Notification;

public interface EmailSender {
    void send(Notification notification);
}
