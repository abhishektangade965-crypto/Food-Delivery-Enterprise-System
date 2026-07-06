package com.fooddelivery.notification.application.service;

import com.fooddelivery.common.domain.exception.DomainException;
import com.fooddelivery.notification.application.config.NotificationTemplateConfig;
import com.fooddelivery.notification.application.dto.NotificationPreferenceRequest;
import com.fooddelivery.notification.application.dto.NotificationResponse;
import com.fooddelivery.notification.application.dto.SendNotificationRequest;
import com.fooddelivery.notification.domain.entity.Notification;
import com.fooddelivery.notification.domain.entity.NotificationPreference;
import com.fooddelivery.notification.domain.port.output.channel.EmailSender;
import com.fooddelivery.notification.domain.port.output.channel.PushNotificationSender;
import com.fooddelivery.notification.domain.port.output.channel.SmsSender;
import com.fooddelivery.notification.domain.port.output.channel.WhatsAppSender;
import com.fooddelivery.notification.domain.port.output.repository.NotificationRepository;
import com.fooddelivery.notification.domain.service.NotificationDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationApplicationServiceImpl implements NotificationApplicationService {

    private final NotificationDomainService notificationDomainService;
    private final NotificationRepository notificationRepository;
    private final SmsSender smsSender;
    private final EmailSender emailSender;
    private final PushNotificationSender pushNotificationSender;
    private final WhatsAppSender whatsappSender;
    private final NotificationTemplateConfig templateConfig;

    @Value("${notification-service.max-retries:3}")
    private int maxRetries;

    @Value("${notification-service.backoff-ms:1000}")
    private long backoffMs;

    @Override
    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        log.info("Received request to send notification type: {} to user: {}", request.getType(), request.getRecipientId());

        if (!isChannelEnabled(request.getRecipientId(), request.getType())) {
            log.warn("Notification channel {} is disabled for user {}", request.getType(), request.getRecipientId());
            Notification notification = Notification.builder()
                    .recipientId(request.getRecipientId())
                    .recipientEmail(request.getRecipientEmail())
                    .recipientPhone(request.getRecipientPhone())
                    .recipientDeviceToken(request.getRecipientDeviceToken())
                    .type(request.getType())
                    .templateName(request.getTemplateName())
                    .templateVariables(request.getTemplateVariables())
                    .build();
            notification.initializeNotification();
            notification.markFailed("CHANNEL_DISABLED_BY_USER_PREFERENCE", 1);
            Notification saved = notificationRepository.save(notification);
            return mapToResponse(saved);
        }

        NotificationTemplateConfig.Template template = templateConfig.getTemplates().get(request.getTemplateName());
        if (template == null) {
            throw new DomainException("Notification template '" + request.getTemplateName() + "' not found in config");
        }

        Notification notification = Notification.builder()
                .recipientId(request.getRecipientId())
                .recipientEmail(request.getRecipientEmail())
                .recipientPhone(request.getRecipientPhone())
                .recipientDeviceToken(request.getRecipientDeviceToken())
                .type(request.getType())
                .templateName(request.getTemplateName())
                .templateVariables(request.getTemplateVariables())
                .build();

        notification.initializeNotification();
        notificationDomainService.validateAndRender(notification, template.getTitle(), template.getBody());
        notificationRepository.save(notification);

        boolean sentSuccessfully = false;
        String lastFailureReason = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                sendViaChannel(notification);
                sentSuccessfully = true;
                break;
            } catch (Exception e) {
                lastFailureReason = e.getMessage();
                log.warn("Attempt {} failed to send notification ID {}: {}", attempt, notification.getId().getValue(), lastFailureReason);
                notification.markFailed(lastFailureReason, maxRetries);
                notificationRepository.save(notification);
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(backoffMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        if (sentSuccessfully) {
            notification.markSent(ZonedDateTime.now(ZoneId.of("UTC")));
            log.info("Notification ID {} sent successfully", notification.getId().getValue());
        } else {
            log.error("Notification ID {} failed permanently after {} attempts", notification.getId().getValue(), maxRetries);
        }

        Notification finalSaved = notificationRepository.save(notification);
        return mapToResponse(finalSaved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationLogs(UUID recipientId) {
        log.info("Fetching notification logs for recipient: {}", recipientId);
        return notificationRepository.findByRecipientId(recipientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotificationLogs() {
        log.info("Fetching all notification logs");
        return notificationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationPreference updatePreferences(UUID userId, NotificationPreferenceRequest request) {
        log.info("Updating preferences for user: {}", userId);
        NotificationPreference preference = notificationRepository.findPreferenceByUserId(userId)
                .orElseGet(() -> NotificationPreference.builder().userId(userId).build());
        preference.setEmailEnabled(request.isEmailEnabled());
        preference.setSmsEnabled(request.isSmsEnabled());
        preference.setPushEnabled(request.isPushEnabled());
        preference.setWhatsappEnabled(request.isWhatsappEnabled());
        return notificationRepository.savePreference(preference);
    }

    @Override
    @Transactional
    public NotificationPreference getPreferences(UUID userId) {
        log.info("Fetching preferences for user: {}", userId);
        return notificationRepository.findPreferenceByUserId(userId)
                .orElseGet(() -> notificationRepository.savePreference(NotificationPreference.getDefault(userId)));
    }

    private boolean isChannelEnabled(UUID userId, com.fooddelivery.notification.domain.valueobject.NotificationType type) {
        NotificationPreference preference = notificationRepository.findPreferenceByUserId(userId)
                .orElseGet(() -> NotificationPreference.getDefault(userId));
        return switch (type) {
            case EMAIL -> preference.isEmailEnabled();
            case SMS -> preference.isSmsEnabled();
            case PUSH -> preference.isPushEnabled();
            case WHATSAPP -> preference.isWhatsappEnabled();
        };
    }

    private void sendViaChannel(Notification notification) {
        switch (notification.getType()) {
            case EMAIL -> emailSender.send(notification);
            case SMS -> smsSender.send(notification);
            case PUSH -> pushNotificationSender.send(notification);
            case WHATSAPP -> whatsappSender.send(notification);
            default -> throw new DomainException("Unsupported notification channel: " + notification.getType());
        }
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getId() != null ? n.getId().getValue() : null)
                .recipientId(n.getRecipientId())
                .recipientEmail(n.getRecipientEmail())
                .recipientPhone(n.getRecipientPhone())
                .recipientDeviceToken(n.getRecipientDeviceToken())
                .type(n.getType())
                .title(n.getTitle())
                .body(n.getBody())
                .status(n.getStatus())
                .sentAt(n.getSentAt())
                .retryCount(n.getRetryCount())
                .failureReason(n.getFailureReason())
                .templateName(n.getTemplateName())
                .templateVariables(n.getTemplateVariables())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
