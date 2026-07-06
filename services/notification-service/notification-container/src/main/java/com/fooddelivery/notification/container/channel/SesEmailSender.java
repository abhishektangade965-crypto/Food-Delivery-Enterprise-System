package com.fooddelivery.notification.container.channel;

import com.fooddelivery.notification.domain.entity.Notification;
import com.fooddelivery.notification.domain.port.output.channel.EmailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SesEmailSender implements EmailSender {

    @Override
    public void send(Notification notification) {
        log.info("Sending Email via AWS SES to {}. Subject: {}, Body: {}",
                notification.getRecipientEmail(), notification.getTitle(), notification.getBody());
        
        if (notification.getRecipientEmail() != null && notification.getRecipientEmail().startsWith("fail")) {
            throw new RuntimeException("AWS SES API failed: ThrottlingException - Rate exceeded");
        }
    }
}
