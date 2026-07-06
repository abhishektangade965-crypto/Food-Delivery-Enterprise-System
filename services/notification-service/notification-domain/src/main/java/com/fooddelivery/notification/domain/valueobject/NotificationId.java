package com.fooddelivery.notification.domain.valueobject;

import com.fooddelivery.common.domain.valueobject.BaseId;
import java.util.UUID;

public class NotificationId extends BaseId<UUID> {
    public NotificationId(UUID value) {
        super(value);
    }
}
