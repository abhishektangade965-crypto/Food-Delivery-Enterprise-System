package com.fooddelivery.order.domain.service;

import com.fooddelivery.order.domain.entity.Order;
import com.fooddelivery.order.domain.entity.Restaurant;
import com.fooddelivery.order.domain.event.OrderCancelledEvent;
import com.fooddelivery.order.domain.event.OrderCreatedEvent;

import java.util.List;

public interface OrderDomainService {
    OrderCreatedEvent validateAndInitializeOrder(Order order, Restaurant restaurant);
    void payOrder(Order order);
    void approveOrder(Order order);
    void cancelOrderPayment(Order order, List<String> failureMessages);
    OrderCancelledEvent cancelOrder(Order order, List<String> failureMessages);
}
