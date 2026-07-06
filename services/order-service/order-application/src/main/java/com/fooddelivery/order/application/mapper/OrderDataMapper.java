package com.fooddelivery.order.application.mapper;

import com.fooddelivery.common.domain.valueobject.CustomerId;
import com.fooddelivery.common.domain.valueobject.Money;
import com.fooddelivery.common.domain.valueobject.ProductId;
import com.fooddelivery.common.domain.valueobject.RestaurantId;
import com.fooddelivery.order.application.dto.CreateOrderCommand;
import com.fooddelivery.order.application.dto.CreateOrderResponse;
import com.fooddelivery.order.application.dto.OrderItemDto;
import com.fooddelivery.order.application.dto.TrackOrderResponse;
import com.fooddelivery.order.domain.entity.Order;
import com.fooddelivery.order.domain.entity.OrderItem;
import com.fooddelivery.order.domain.entity.Product;
import com.fooddelivery.order.domain.entity.Restaurant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class OrderDataMapper {

    public Restaurant createOrderCommandToRestaurant(CreateOrderCommand createOrderCommand) {
        return Restaurant.builder()
                .products(createOrderCommand.items().stream()
                        .map(item -> Product.builder()
                                .build())
                        .collect(Collectors.toList()))
                .active(true)
                .build();
    }

    public Order createOrderCommandToOrder(CreateOrderCommand createOrderCommand) {
        return Order.builder()
                .customerId(new CustomerId(createOrderCommand.customerId()))
                .restaurantId(new RestaurantId(createOrderCommand.restaurantId()))
                .deliveryAddress(createOrderCommand.deliveryAddress())
                .price(new Money(createOrderCommand.price()))
                .items(createOrderCommand.items().stream()
                        .map(this::orderItemDtoToOrderItem)
                        .collect(Collectors.toList()))
                .promoCode(createOrderCommand.promoCode())
                .specialInstructions(createOrderCommand.specialInstructions())
                .build();
    }

    private OrderItem orderItemDtoToOrderItem(OrderItemDto itemDto) {
        return OrderItem.builder()
                .product(Product.builder()
                        .build())
                .quantity(itemDto.quantity())
                .price(new Money(itemDto.price()))
                .subTotal(new Money(itemDto.subTotal()))
                .build();
    }

    public CreateOrderResponse orderToCreateOrderResponse(Order order, String message) {
        return CreateOrderResponse.builder()
                .orderTrackingId(order.getTrackingId().getValue())
                .orderStatus(order.getOrderStatus().name())
                .message(message)
                .build();
    }

    public TrackOrderResponse orderToTrackOrderResponse(Order order) {
        return TrackOrderResponse.builder()
                .orderTrackingId(order.getTrackingId().getValue())
                .orderStatus(order.getOrderStatus().name())
                .failureMessages(order.getFailureMessages())
                .build();
    }
}
