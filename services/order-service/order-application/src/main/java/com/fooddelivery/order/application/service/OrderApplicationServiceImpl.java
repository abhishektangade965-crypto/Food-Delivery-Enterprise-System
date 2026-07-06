package com.fooddelivery.order.application.service;

import com.fooddelivery.order.application.dto.CreateOrderCommand;
import com.fooddelivery.order.application.dto.CreateOrderResponse;
import com.fooddelivery.order.application.dto.TrackOrderQuery;
import com.fooddelivery.order.application.dto.TrackOrderResponse;
import com.fooddelivery.order.application.mapper.OrderDataMapper;
import com.fooddelivery.order.domain.entity.Order;
import com.fooddelivery.order.domain.entity.Restaurant;
import com.fooddelivery.order.domain.event.OrderCreatedEvent;
import com.fooddelivery.order.domain.exception.OrderDomainException;
import com.fooddelivery.order.domain.exception.OrderNotFoundException;
import com.fooddelivery.order.domain.port.output.message.publisher.OrderCreatedPaymentRequestMessagePublisher;
import com.fooddelivery.order.domain.port.output.repository.OrderRepository;
import com.fooddelivery.order.domain.port.output.repository.RestaurantRepository;
import com.fooddelivery.order.domain.service.OrderDomainService;
import com.fooddelivery.order.domain.valueobject.TrackingId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderApplicationServiceImpl implements OrderApplicationService {

    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderDataMapper orderDataMapper;
    private final OrderCreatedPaymentRequestMessagePublisher orderCreatedPaymentRequestMessagePublisher;

    @Override
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        Restaurant restaurant = orderDataMapper.createOrderCommandToRestaurant(createOrderCommand);
        Optional<Restaurant> restaurantOptional = restaurantRepository.findRestaurantInformation(restaurant);
        if (restaurantOptional.isEmpty()) {
            log.warn("Could not find restaurant with id: {}", createOrderCommand.restaurantId());
            throw new OrderDomainException("Could not find restaurant with id: " + createOrderCommand.restaurantId());
        }

        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        OrderCreatedEvent orderCreatedEvent = orderDomainService.validateAndInitializeOrder(order, restaurantOptional.get());
        Order savedOrder = orderRepository.save(order);
        log.info("Order is created with id: {}", savedOrder.getId().getValue());

        orderCreatedPaymentRequestMessagePublisher.publish(orderCreatedEvent);
        return orderDataMapper.orderToCreateOrderResponse(savedOrder, "Order created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        return orderRepository.findByTrackingId(new TrackingId(trackOrderQuery.trackingId()))
                .map(orderDataMapper::orderToTrackOrderResponse)
                .orElseThrow(() -> {
                    log.warn("Could not find order with tracking id: {}", trackOrderQuery.trackingId());
                    return new OrderNotFoundException("Could not find order with tracking id: " + trackOrderQuery.trackingId());
                });
    }
}
