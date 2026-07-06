package com.fooddelivery.notification.container.channel;

import com.fooddelivery.notification.domain.entity.Notification;
import com.fooddelivery.notification.domain.port.output.channel.PushNotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FcmPushNotificationSender implements PushNotificationSender {

    @Override
    public void send(Notification notification) {
        log.info("Sending Push Notification via Firebase Cloud Messaging (FCM) to token {}. Title: {}, Body: {}",
                notification.getRecipientDeviceToken(), notification.getTitle(), notification.getBody());
        
        if ("fail-token".equalsIgnoreCase(notification.getRecipientDeviceToken())) {
            throw new RuntimeException("FCM API failed: MessagingException - Device token expired or invalid");
        }
    }
}
