package com.fooddelivery.order.domain.service;

import com.fooddelivery.order.domain.entity.Order;
import com.fooddelivery.order.domain.entity.Restaurant;
import com.fooddelivery.order.domain.event.OrderCancelledEvent;
import com.fooddelivery.order.domain.event.OrderCreatedEvent;
import com.fooddelivery.order.domain.exception.OrderDomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderDomainServiceImpl implements OrderDomainService {

    @Override
    public OrderCreatedEvent validateAndInitializeOrder(Order order, Restaurant restaurant) {
        validateRestaurant(restaurant);
        order.initializeOrder();
        log.info("Order with id: {} has been initialized", order.getId().getValue());
        return new OrderCreatedEvent(order, ZonedDateTime.now(ZoneId.of("UTC")));
    }

    @Override
    public void payOrder(Order order) {
        order.pay();
        log.info("Order with id: {} has been paid", order.getId().getValue());
    }

    @Override
    public void approveOrder(Order order) {
        order.approve();
        log.info("Order with id: {} has been approved", order.getId().getValue());
    }

    @Override
    public void cancelOrderPayment(Order order, List<String> failureMessages) {
        order.initCancel(failureMessages);
        log.info("Order payment cancellation is initiating for order id: {}", order.getId().getValue());
    }

    @Override
    public OrderCancelledEvent cancelOrder(Order order, List<String> failureMessages) {
        order.cancel(failureMessages);
        log.info("Order with id: {} has been cancelled", order.getId().getValue());
        return new OrderCancelledEvent(order, ZonedDateTime.now(ZoneId.of("UTC")));
    }

    private void validateRestaurant(Restaurant restaurant) {
        if (!restaurant.isActive()) {
            throw new OrderDomainException("Restaurant with id " + restaurant.getId().getValue() +
                    " is not active and cannot accept orders!");
        }
    }
}
