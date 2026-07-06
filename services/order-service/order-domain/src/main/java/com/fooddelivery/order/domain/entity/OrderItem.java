package com.fooddelivery.order.domain.entity;

import com.fooddelivery.common.domain.entity.BaseEntity;
import com.fooddelivery.common.domain.valueobject.Money;
import com.fooddelivery.common.domain.valueobject.OrderId;
import com.fooddelivery.order.domain.valueobject.OrderItemId;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderItem extends BaseEntity<OrderItemId> {
    private OrderId orderId;
    private final Product product;
    private final int quantity;
    private final Money price;
    private final Money subTotal;

    public void initializeOrderItem(OrderId orderId, OrderItemId orderItemId) {
        this.orderId = orderId;
        setId(orderItemId);
    }
}
