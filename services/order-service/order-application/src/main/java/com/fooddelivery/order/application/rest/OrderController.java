package com.fooddelivery.order.application.rest;

import com.fooddelivery.order.application.dto.CreateOrderCommand;
import com.fooddelivery.order.application.dto.CreateOrderResponse;
import com.fooddelivery.order.application.dto.TrackOrderQuery;
import com.fooddelivery.order.application.dto.TrackOrderResponse;
import com.fooddelivery.order.application.service.OrderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order placements and tracking")
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    @PostMapping
    @Operation(summary = "Place a new order")
    @ApiResponse(responseCode = "200", description = "Order placed successfully")
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody @Valid CreateOrderCommand createOrderCommand) {
        log.info("Creating order for customer: {} at restaurant: {}", createOrderCommand.customerId(), createOrderCommand.restaurantId());
        CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderCommand);
        return ResponseEntity.ok(createOrderResponse);
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Track order status by tracking token")
    @ApiResponse(responseCode = "200", description = "Order details fetched successfully")
    public ResponseEntity<TrackOrderResponse> getOrderByTrackingId(@PathVariable UUID trackingId) {
        log.info("Tracking order with tracking id: {}", trackingId);
        TrackOrderResponse trackOrderResponse = orderApplicationService.trackOrder(new TrackOrderQuery(trackingId));
        return ResponseEntity.ok(trackOrderResponse);
    }
}
