package com.fooddelivery.user.domain;

import com.fooddelivery.user.domain.entity.User;
import com.fooddelivery.user.domain.valueobject.UserStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserTest {

    @Test
    public void testUserInitialization() {
        User user = User.builder()
                .email("test@fooddelivery.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        user.initializeUser();

        assertNotNull(user.getId());
        assertEquals(UserStatus.PENDING_VERIFICATION, user.getStatus());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    public void testUserActivation() {
        User user = User.builder()
                .email("test@fooddelivery.com")
                .status(UserStatus.PENDING_VERIFICATION)
                .build();

        user.activate();

        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    public void testDeletedUserActivationFails() {
        User user = User.builder()
                .email("test@fooddelivery.com")
                .status(UserStatus.DELETED)
                .build();

        assertThrows(IllegalStateException.class, user::activate);
    }
}
