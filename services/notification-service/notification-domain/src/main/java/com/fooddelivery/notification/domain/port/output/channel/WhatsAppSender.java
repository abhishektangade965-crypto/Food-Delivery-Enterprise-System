package com.fooddelivery.notification.domain.port.output.channel;

import com.fooddelivery.notification.domain.entity.Notification;

public interface WhatsAppSender {
    void send(Notification notification);
}
