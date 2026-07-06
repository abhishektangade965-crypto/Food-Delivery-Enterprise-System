package com.fooddelivery.user.application.mapper;

import com.fooddelivery.user.application.dto.RegisterUserRequest;
import com.fooddelivery.user.application.dto.UserResponse;
import com.fooddelivery.user.domain.entity.User;
import com.fooddelivery.user.domain.valueobject.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "preferences", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "mfaEnabled", ignore = true)
    @Mapping(target = "mfaSecret", ignore = true)
    @Mapping(target = "deviceTokens", ignore = true)
    @Mapping(target = "walletId", ignore = true)
    User registerRequestToUser(RegisterUserRequest request);

    @Mapping(target = "userId", expression = "java(user.getId().getValue().toString())")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoles")
    UserResponse userToUserResponse(User user);

    @Named("mapRoles")
    default Set<String> mapRoles(Set<UserRole> roles) {
        if (roles == null) return Set.of();
        return roles.stream().map(Enum::name).collect(Collectors.toSet());
    }
}
