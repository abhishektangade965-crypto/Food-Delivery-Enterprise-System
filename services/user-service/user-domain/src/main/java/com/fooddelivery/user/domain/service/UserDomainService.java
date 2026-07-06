package com.fooddelivery.user.domain.service;

import com.fooddelivery.user.domain.entity.User;
import com.fooddelivery.user.domain.event.UserActivatedEvent;
import com.fooddelivery.user.domain.event.UserCreatedEvent;
import com.fooddelivery.user.domain.event.UserSuspendedEvent;

public interface UserDomainService {
    UserCreatedEvent createUser(User user);
    UserActivatedEvent activateUser(User user);
    UserSuspendedEvent suspendUser(User user);
    boolean validateUserCredentials(User user, String passwordPlain);
}
