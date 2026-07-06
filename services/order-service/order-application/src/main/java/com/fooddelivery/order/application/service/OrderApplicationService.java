package com.fooddelivery.order.application.service;

import com.fooddelivery.order.application.dto.CreateOrderCommand;
import com.fooddelivery.order.application.dto.CreateOrderResponse;
import com.fooddelivery.order.application.dto.TrackOrderQuery;
import com.fooddelivery.order.application.dto.TrackOrderResponse;

public interface OrderApplicationService {
    CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand);
    TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery);
}
