package com.fooddelivery.notification.container.channel;

import com.fooddelivery.notification.domain.entity.Notification;
import com.fooddelivery.notification.domain.port.output.channel.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TwilioSmsSender implements SmsSender {

    @Override
    public void send(Notification notification) {
        log.info("Sending SMS via Twilio to {}. Title: {}, Body: {}",
                notification.getRecipientPhone(), notification.getTitle(), notification.getBody());
        
        if (notification.getRecipientPhone() != null && notification.getRecipientPhone().contains("9999")) {
            throw new RuntimeException("Twilio API failed: Network timeout connection refused");
        }
    }
}
