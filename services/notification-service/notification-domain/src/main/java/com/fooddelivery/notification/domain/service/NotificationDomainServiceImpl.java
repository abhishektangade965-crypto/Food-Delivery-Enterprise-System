package com.fooddelivery.notification.domain.service;

import com.fooddelivery.common.domain.exception.DomainException;
import com.fooddelivery.notification.domain.entity.Notification;
import com.fooddelivery.notification.domain.valueobject.NotificationType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class NotificationDomainServiceImpl implements NotificationDomainService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{8,15}$");

    @Override
    public void validateAndRender(Notification notification, String templateTitle, String templateBody) {
        log.info("Validating and rendering notification for recipientId: {}, type: {}",
                notification.getRecipientId(), notification.getType());

        validateRecipientDetails(notification);

        String renderedTitle = renderText(templateTitle, notification.getTemplateVariables());
        String renderedBody = renderText(templateBody, notification.getTemplateVariables());

        notification.renderContent(renderedTitle, renderedBody);
    }

    @Override
    public boolean canRetry(Notification notification, int maxRetryAttempts) {
        return notification.getRetryCount() < maxRetryAttempts;
    }

    private void validateRecipientDetails(Notification notification) {
        NotificationType type = notification.getType();
        if (type == null) {
            throw new DomainException("Notification type cannot be null");
        }

        switch (type) {
            case EMAIL -> {
                String email = notification.getRecipientEmail();
                if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
                    throw new DomainException("Invalid recipient email: " + email);
                }
            }
            case SMS, WHATSAPP -> {
                String phone = notification.getRecipientPhone();
                if (phone == null || phone.isBlank() || !PHONE_PATTERN.matcher(phone).matches()) {
                    throw new DomainException("Invalid recipient phone: " + phone);
                }
            }
            case PUSH -> {
                String token = notification.getRecipientDeviceToken();
                if (token == null || token.isBlank()) {
                    throw new DomainException("Recipient device token is required for push notifications");
                }
            }
            default -> throw new DomainException("Unsupported notification type: " + type);
        }
    }

    private String renderText(String template, Map<String, String> variables) {
        if (template == null || template.isBlank()) {
            return "";
        }
        if (variables == null || variables.isEmpty()) {
            return template;
        }
        String rendered = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() == null ? "" : entry.getValue();
            rendered = rendered.replace("{{" + key + "}}", value)
                               .replace("{" + key + "}", value);
        }
        return rendered;
    }
}
