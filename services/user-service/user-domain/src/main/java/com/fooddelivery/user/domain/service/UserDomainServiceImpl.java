package com.fooddelivery.user.domain.service;

import com.fooddelivery.user.domain.entity.User;
import com.fooddelivery.user.domain.event.UserActivatedEvent;
import com.fooddelivery.user.domain.event.UserCreatedEvent;
import com.fooddelivery.user.domain.event.UserSuspendedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
public class UserDomainServiceImpl implements UserDomainService {

    @Override
    public UserCreatedEvent createUser(User user) {
        user.initializeUser();
        log.info("Initialized user with email: {}", user.getEmail());
        return new UserCreatedEvent(user, ZonedDateTime.now(ZoneId.of("UTC")));
    }

    @Override
    public UserActivatedEvent activateUser(User user) {
        user.activate();
        log.info("Activated user: {}", user.getId().getValue());
        return new UserActivatedEvent(user, ZonedDateTime.now(ZoneId.of("UTC")));
    }

    @Override
    public UserSuspendedEvent suspendUser(User user) {
        user.suspend();
        log.info("Suspended user: {}", user.getId().getValue());
        return new UserSuspendedEvent(user, ZonedDateTime.now(ZoneId.of("UTC")));
    }

    @Override
    public boolean validateUserCredentials(User user, String passwordPlain) {
        // Credential validation logic will be handled by the BCrypt encoder in the Application Service layer.
        return user.getPasswordHash() != null && !user.getPasswordHash().trim().isEmpty();
    }
}
