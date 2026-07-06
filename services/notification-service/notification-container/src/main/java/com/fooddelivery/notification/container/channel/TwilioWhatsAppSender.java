package com.fooddelivery.notification.container.channel;

import com.fooddelivery.notification.domain.entity.Notification;
import com.fooddelivery.notification.domain.port.output.channel.WhatsAppSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TwilioWhatsAppSender implements WhatsAppSender {

    @Override
    public void send(Notification notification) {
        log.info("Sending WhatsApp message via Twilio to {}. Title: {}, Body: {}",
                notification.getRecipientPhone(), notification.getTitle(), notification.getBody());
        
        if (notification.getRecipientPhone() != null && notification.getRecipientPhone().contains("8888")) {
            throw new RuntimeException("Twilio WhatsApp API failed: rate limit exceeded");
        }
    }
}
