package com.fooddelivery.notification.dataaccess.mapper;

import com.fooddelivery.notification.dataaccess.entity.NotificationEntity;
import com.fooddelivery.notification.dataaccess.entity.NotificationPreferenceEntity;
import com.fooddelivery.notification.domain.entity.Notification;
import com.fooddelivery.notification.domain.entity.NotificationPreference;
import com.fooddelivery.notification.domain.valueobject.NotificationId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface NotificationDataAccessMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "notificationIdToUuid")
    NotificationEntity notificationToNotificationEntity(Notification notification);

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToNotificationId")
    Notification notificationEntityToNotification(NotificationEntity notificationEntity);

    NotificationPreferenceEntity preferenceToPreferenceEntity(NotificationPreference preference);

    NotificationPreference preferenceEntityToPreference(NotificationPreferenceEntity preferenceEntity);

    @Named("notificationIdToUuid")
    default UUID notificationIdToUuid(NotificationId notificationId) {
        return notificationId == null ? null : notificationId.getValue();
    }

    @Named("uuidToNotificationId")
    default NotificationId uuidToNotificationId(UUID uuid) {
        return uuid == null ? null : new NotificationId(uuid);
    }
}
