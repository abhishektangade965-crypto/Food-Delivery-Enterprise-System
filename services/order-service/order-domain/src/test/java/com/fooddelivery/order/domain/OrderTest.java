package com.fooddelivery.order.domain;

import com.fooddelivery.common.domain.valueobject.OrderStatus;
import com.fooddelivery.order.domain.entity.Order;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderTest {

    @Test
    public void testOrderInitialization() {
        Order order = Order.builder()
                .items(new ArrayList<>())
                .build();

        order.initializeOrder();

        assertNotNull(order.getId());
        assertNotNull(order.getTrackingId());
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
    }

    @Test
    public void testOrderPaymentTransition() {
        Order order = Order.builder()
                .items(new ArrayList<>())
                .orderStatus(OrderStatus.PENDING)
                .build();

        order.pay();

        assertEquals(OrderStatus.PAID, order.getOrderStatus());
    }

    @Test
    public void testInvalidOrderTransitionFails() {
        Order order = Order.builder()
                .items(new ArrayList<>())
                .orderStatus(OrderStatus.APPROVED)
                .build();

        assertThrows(IllegalStateException.class, order::pay);
    }
}
