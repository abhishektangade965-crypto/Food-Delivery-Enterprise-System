package com.fooddelivery.user.domain.entity;

import com.fooddelivery.common.domain.entity.BaseEntity;
import com.fooddelivery.common.domain.valueobject.Address;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class UserAddress extends BaseEntity<UUID> {
    private final UUID userId;
    private String label;
    private Address address;
    private boolean isDefault;

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
