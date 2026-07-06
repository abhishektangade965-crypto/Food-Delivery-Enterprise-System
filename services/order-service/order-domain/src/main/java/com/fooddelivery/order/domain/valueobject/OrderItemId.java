package com.fooddelivery.order.domain.valueobject;

import com.fooddelivery.common.domain.valueobject.BaseId;

public class OrderItemId extends BaseId<Long> {
    public OrderItemId(Long value) {
        super(value);
    }
}
