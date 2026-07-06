package com.fooddelivery.notification.application.rest;

import com.fooddelivery.notification.application.dto.NotificationPreferenceRequest;
import com.fooddelivery.notification.application.dto.NotificationResponse;
import com.fooddelivery.notification.application.dto.SendNotificationRequest;
import com.fooddelivery.notification.application.service.NotificationApplicationService;
import com.fooddelivery.notification.domain.entity.NotificationPreference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notifications management and dispatching")
public class NotificationController {

    private final NotificationApplicationService notificationApplicationService;

    @PostMapping
    @Operation(summary = "Send a dynamic template-based notification")
    @ApiResponse(responseCode = "200", description = "Notification dispatch evaluated and saved")
    public ResponseEntity<NotificationResponse> sendNotification(@RequestBody @Valid SendNotificationRequest request) {
        log.info("REST request to send notification to recipientId: {}", request.getRecipientId());
        NotificationResponse response = notificationApplicationService.sendNotification(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logs")
    @Operation(summary = "Fetch all notification logs")
    public ResponseEntity<List<NotificationResponse>> getAllLogs() {
        log.info("REST request to fetch all notification logs");
        List<NotificationResponse> logs = notificationApplicationService.getAllNotificationLogs();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/{recipientId}")
    @Operation(summary = "Fetch notification logs for a specific recipient")
    public ResponseEntity<List<NotificationResponse>> getLogsByRecipient(@PathVariable UUID recipientId) {
        log.info("REST request to fetch logs for recipientId: {}", recipientId);
        List<NotificationResponse> logs = notificationApplicationService.getNotificationLogs(recipientId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/preferences/{userId}")
    @Operation(summary = "Fetch notification preferences for a user")
    public ResponseEntity<NotificationPreference> getPreferences(@PathVariable UUID userId) {
        log.info("REST request to fetch preferences for userId: {}", userId);
        NotificationPreference preference = notificationApplicationService.getPreferences(userId);
        return ResponseEntity.ok(preference);
    }

    @PutMapping("/preferences/{userId}")
    @Operation(summary = "Update notification preferences for a user")
    public ResponseEntity<NotificationPreference> updatePreferences(
            @PathVariable UUID userId,
            @RequestBody @Valid NotificationPreferenceRequest request) {
        log.info("REST request to update preferences for userId: {}", userId);
        NotificationPreference preference = notificationApplicationService.updatePreferences(userId, request);
        return ResponseEntity.ok(preference);
    }
}
