package com.fooddelivery.order.dataaccess.mapper;

import com.fooddelivery.common.domain.valueobject.Address;
import com.fooddelivery.common.domain.valueobject.CustomerId;
import com.fooddelivery.common.domain.valueobject.Money;
import com.fooddelivery.common.domain.valueobject.OrderId;
import com.fooddelivery.common.domain.valueobject.ProductId;
import com.fooddelivery.common.domain.valueobject.RestaurantId;
import com.fooddelivery.order.dataaccess.entity.OrderEntity;
import com.fooddelivery.order.dataaccess.entity.OrderItemEntity;
import com.fooddelivery.order.dataaccess.entity.RestaurantEntity;
import com.fooddelivery.order.domain.entity.Order;
import com.fooddelivery.order.domain.entity.OrderItem;
import com.fooddelivery.order.domain.entity.Product;
import com.fooddelivery.order.domain.entity.Restaurant;
import com.fooddelivery.order.domain.valueobject.OrderItemId;
import com.fooddelivery.order.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderDataAccessMapper {

    public OrderEntity orderToOrderEntity(Order order) {
        OrderEntity orderEntity = OrderEntity.builder()
                .id(order.getId().getValue())
                .customerId(order.getCustomerId().getValue())
                .restaurantId(order.getRestaurantId().getValue())
                .trackingId(order.getTrackingId().getValue())
                .price(order.getPrice().getAmount())
                .orderStatus(order.getOrderStatus())
                .failureMessages(order.getFailureMessages() != null ? String.join(",", order.getFailureMessages()) : "")
                .street(order.getDeliveryAddress().street())
                .city(order.getDeliveryAddress().city())
                .state(order.getDeliveryAddress().state())
                .country(order.getDeliveryAddress().country())
                .postalCode(order.getDeliveryAddress().postalCode())
                .latitude(order.getDeliveryAddress().latitude())
                .longitude(order.getDeliveryAddress().longitude())
                .build();

        orderEntity.setItems(order.getItems().stream()
                .map(item -> orderItemToOrderItemEntity(item, orderEntity))
                .collect(Collectors.toList()));

        return orderEntity;
    }

    private OrderItemEntity orderItemToOrderItemEntity(OrderItem item, OrderEntity orderEntity) {
        return OrderItemEntity.builder()
                .id(item.getId().getValue())
                .order(orderEntity)
                .productId(item.getProduct().getId().getValue())
                .quantity(item.getQuantity())
                .price(item.getPrice().getAmount())
                .subTotal(item.getSubTotal().getAmount())
                .build();
    }

    public Order orderEntityToOrder(OrderEntity entity) {
        return Order.builder()
                .customerId(new CustomerId(entity.getCustomerId()))
                .restaurantId(new RestaurantId(entity.getRestaurantId()))
                .deliveryAddress(new Address(entity.getStreet(), entity.getCity(), entity.getState(), entity.getCountry(), entity.getPostalCode(), entity.getLatitude(), entity.getLongitude()))
                .price(new Money(entity.getPrice()))
                .items(entity.getItems().stream()
                        .map(this::orderItemEntityToOrderItem)
                        .collect(Collectors.toList()))
                .trackingId(new TrackingId(entity.getTrackingId()))
                .orderStatus(entity.getOrderStatus())
                .failureMessages(entity.getFailureMessages() != null && !entity.getFailureMessages().trim().isEmpty() ? List.of(entity.getFailureMessages().split(",")) : new ArrayList<>())
                .build();
    }

    private OrderItem orderItemEntityToOrderItem(OrderItemEntity entity) {
        OrderItem item = OrderItem.builder()
                .product(Product.builder()
                        .build())
                .quantity(entity.getQuantity())
                .price(new Money(entity.getPrice()))
                .subTotal(new Money(entity.getSubTotal()))
                .build();
        item.setId(new OrderItemId(entity.getId()));
        return item;
    }

    public RestaurantEntity restaurantToRestaurantEntity(Restaurant restaurant) {
        return RestaurantEntity.builder()
                .id(restaurant.getId().getValue())
                .active(restaurant.isActive())
                .build();
    }

    public Restaurant restaurantEntityToRestaurant(RestaurantEntity entity) {
        Restaurant restaurant = Restaurant.builder()
                .products(new ArrayList<>())
                .active(entity.isActive())
                .build();
        restaurant.setId(new RestaurantId(entity.getId()));
        return restaurant;
    }
}
