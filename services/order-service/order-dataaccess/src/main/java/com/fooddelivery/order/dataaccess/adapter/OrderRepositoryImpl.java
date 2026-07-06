package com.fooddelivery.order.dataaccess.adapter;

import com.fooddelivery.common.domain.valueobject.CustomerId;
import com.fooddelivery.order.dataaccess.entity.OrderEntity;
import com.fooddelivery.order.dataaccess.mapper.OrderDataAccessMapper;
import com.fooddelivery.order.dataaccess.repository.OrderJpaRepository;
import com.fooddelivery.order.domain.entity.Order;
import com.fooddelivery.order.domain.port.output.repository.OrderRepository;
import com.fooddelivery.order.domain.valueobject.TrackingId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderDataAccessMapper orderDataAccessMapper;

    @Override
    public Order save(Order order) {
        OrderEntity entity = orderDataAccessMapper.orderToOrderEntity(order);
        OrderEntity saved = orderJpaRepository.save(entity);
        return orderDataAccessMapper.orderEntityToOrder(saved);
    }

    @Override
    public Optional<Order> findByTrackingId(TrackingId trackingId) {
        return orderJpaRepository.findByTrackingId(trackingId.getValue())
                .map(orderDataAccessMapper::orderEntityToOrder);
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return orderJpaRepository.findByCustomerId(customerId.getValue()).stream()
                .map(orderDataAccessMapper::orderEntityToOrder)
                .collect(Collectors.toList());
    }
}
