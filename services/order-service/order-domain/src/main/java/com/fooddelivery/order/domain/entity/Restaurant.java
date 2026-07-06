package com.fooddelivery.order.domain.entity;

import com.fooddelivery.common.domain.entity.AggregateRoot;
import com.fooddelivery.common.domain.valueobject.RestaurantId;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Restaurant extends AggregateRoot<RestaurantId> {
    private final List<Product> products;
    private boolean active;
}
