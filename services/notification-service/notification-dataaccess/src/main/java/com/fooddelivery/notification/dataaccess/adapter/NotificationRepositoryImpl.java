package com.fooddelivery.notification.dataaccess.adapter;

import com.fooddelivery.notification.dataaccess.entity.NotificationEntity;
import com.fooddelivery.notification.dataaccess.entity.NotificationPreferenceEntity;
import com.fooddelivery.notification.dataaccess.mapper.NotificationDataAccessMapper;
import com.fooddelivery.notification.dataaccess.repository.NotificationJpaRepository;
import com.fooddelivery.notification.dataaccess.repository.NotificationPreferenceJpaRepository;
import com.fooddelivery.notification.domain.entity.Notification;
import com.fooddelivery.notification.domain.entity.NotificationPreference;
import com.fooddelivery.notification.domain.port.output.repository.NotificationRepository;
import com.fooddelivery.notification.domain.valueobject.NotificationId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationPreferenceJpaRepository notificationPreferenceJpaRepository;
    private final NotificationDataAccessMapper mapper;

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = mapper.notificationToNotificationEntity(notification);
        NotificationEntity saved = notificationJpaRepository.save(entity);
        return mapper.notificationEntityToNotification(saved);
    }

    @Override
    public Optional<Notification> findById(NotificationId notificationId) {
        return notificationJpaRepository.findById(notificationId.getValue())
                .map(mapper::notificationEntityToNotification);
    }

    @Override
    public List<Notification> findByRecipientId(UUID recipientId) {
        return notificationJpaRepository.findByRecipientId(recipientId).stream()
                .map(mapper::notificationEntityToNotification)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findAll() {
        return notificationJpaRepository.findAll().stream()
                .map(mapper::notificationEntityToNotification)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationPreference savePreference(NotificationPreference preference) {
        NotificationPreferenceEntity entity = mapper.preferenceToPreferenceEntity(preference);
        NotificationPreferenceEntity saved = notificationPreferenceJpaRepository.save(entity);
        return mapper.preferenceEntityToPreference(saved);
    }

    @Override
    public Optional<NotificationPreference> findPreferenceByUserId(UUID userId) {
        return notificationPreferenceJpaRepository.findById(userId)
                .map(mapper::preferenceEntityToPreference);
    }
}
