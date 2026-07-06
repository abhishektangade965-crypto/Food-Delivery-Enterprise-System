package com.fooddelivery.order.domain.port.output.repository;

import com.fooddelivery.common.domain.valueobject.CustomerId;
import com.fooddelivery.order.domain.entity.Order;
import com.fooddelivery.order.domain.valueobject.TrackingId;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findByTrackingId(TrackingId trackingId);
    List<Order> findByCustomerId(CustomerId customerId);
}
