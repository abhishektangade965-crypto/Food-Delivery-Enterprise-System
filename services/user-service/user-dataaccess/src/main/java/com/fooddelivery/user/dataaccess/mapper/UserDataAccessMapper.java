package com.fooddelivery.user.dataaccess.mapper;

import com.fooddelivery.common.domain.valueobject.Address;
import com.fooddelivery.user.dataaccess.entity.UserAddressJpaEntity;
import com.fooddelivery.user.dataaccess.entity.UserJpaEntity;
import com.fooddelivery.user.domain.entity.User;
import com.fooddelivery.user.domain.entity.UserAddress;
import com.fooddelivery.user.domain.valueobject.UserId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class UserDataAccessMapper {

    public UserJpaEntity userToUserJpaEntity(User user) {
        UserJpaEntity userJpaEntity = UserJpaEntity.builder()
                .id(user.getId().getValue())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .dateOfBirth(user.getDateOfBirth())
                .profileImageUrl(user.getProfileImageUrl())
                .status(user.getStatus())
                .roles(new HashSet<>(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .mfaEnabled(user.isMfaEnabled())
                .mfaSecret(user.getMfaSecret())
                .walletId(user.getWalletId())
                .build();

        if (user.getAddresses() != null) {
            userJpaEntity.setAddresses(user.getAddresses().stream()
                    .map(addr -> userAddressToUserAddressJpaEntity(addr, userJpaEntity))
                    .collect(Collectors.toList()));
        }

        return userJpaEntity;
    }

    public User userJpaEntityToUser(UserJpaEntity entity) {
        return User.builder()
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phone(entity.getPhone())
                .dateOfBirth(entity.getDateOfBirth())
                .profileImageUrl(entity.getProfileImageUrl())
                .status(entity.getStatus())
                .roles(entity.getRoles())
                .addresses(entity.getAddresses() != null ? entity.getAddresses().stream()
                        .map(this::userAddressJpaEntityToUserAddress)
                        .collect(Collectors.toList()) : new ArrayList<>())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .lastLoginAt(entity.getLastLoginAt())
                .mfaEnabled(entity.isMfaEnabled())
                .mfaSecret(entity.getMfaSecret())
                .walletId(entity.getWalletId())
                .build();
    }

    private UserAddressJpaEntity userAddressToUserAddressJpaEntity(UserAddress address, UserJpaEntity userEntity) {
        return UserAddressJpaEntity.builder()
                .id(address.getId())
                .user(userEntity)
                .label(address.getLabel())
                .street(address.getAddress().street())
                .city(address.getAddress().city())
                .state(address.getAddress().state())
                .country(address.getAddress().country())
                .postalCode(address.getAddress().postalCode())
                .latitude(address.getAddress().latitude())
                .longitude(address.getAddress().longitude())
                .isDefault(address.isDefault())
                .build();
    }

    private UserAddress userAddressJpaEntityToUserAddress(UserAddressJpaEntity entity) {
        UserAddress address = UserAddress.builder()
                .userId(entity.getUser().getId())
                .label(entity.getLabel())
                .address(new Address(entity.getStreet(), entity.getCity(), entity.getState(), entity.getCountry(), entity.getPostalCode(), entity.getLatitude(), entity.getLongitude()))
                .isDefault(entity.isDefault())
                .build();
        address.setId(entity.getId());
        return address;
    }
}
