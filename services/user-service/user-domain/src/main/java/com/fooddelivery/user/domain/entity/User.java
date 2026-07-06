package com.fooddelivery.user.domain.entity;

import com.fooddelivery.common.domain.entity.AggregateRoot;
import com.fooddelivery.common.domain.valueobject.Money;
import com.fooddelivery.user.domain.valueobject.UserId;
import com.fooddelivery.user.domain.valueobject.UserPreferences;
import com.fooddelivery.user.domain.valueobject.UserRole;
import com.fooddelivery.user.domain.valueobject.UserStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
public class User extends AggregateRoot<UserId> {
    private final String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate dateOfBirth;
    private String profileImageUrl;
    private UserStatus status;
    private Set<UserRole> roles;
    private List<UserAddress> addresses;
    private UserPreferences preferences;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime lastLoginAt;
    private boolean mfaEnabled;
    private String mfaSecret;
    private Set<String> deviceTokens;
    private UUID walletId;

    public void initializeUser() {
        setId(new UserId(UUID.randomUUID()));
        this.status = UserStatus.PENDING_VERIFICATION;
        this.addresses = new ArrayList<>();
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    public void activate() {
        if (this.status == UserStatus.DELETED) {
            throw new IllegalStateException("Deleted user cannot be activated");
        }
        this.status = UserStatus.ACTIVE;
        this.updatedAt = ZonedDateTime.now();
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
        this.updatedAt = ZonedDateTime.now();
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
        this.updatedAt = ZonedDateTime.now();
    }

    public void delete() {
        this.status = UserStatus.DELETED;
        this.updatedAt = ZonedDateTime.now();
    }

    public void addAddress(UserAddress address) {
        if (addresses == null) {
            addresses = new ArrayList<>();
        }
        if (address.isDefault()) {
            addresses.forEach(addr -> addr.setDefault(false));
        }
        addresses.add(address);
    }

    public void removeAddress(UUID addressId) {
        if (addresses != null) {
            addresses.removeIf(address -> address.getId().equals(addressId));
        }
    }

    public void updatePreferences(UserPreferences preferences) {
        this.preferences = preferences;
        this.updatedAt = ZonedDateTime.now();
    }

    public void enableMfa(String secret) {
        this.mfaEnabled = true;
        this.mfaSecret = secret;
        this.updatedAt = ZonedDateTime.now();
    }

    public void disableMfa() {
        this.mfaEnabled = false;
        this.mfaSecret = null;
        this.updatedAt = ZonedDateTime.now();
    }
}
