package com.fooddelivery.delivery.messaging.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.delivery.application.dto.LocationDto;
import com.fooddelivery.delivery.application.service.DeliveryApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class OrderEventKafkaListener {

    private final DeliveryApplicationService deliveryApplicationService;
    private final ObjectMapper objectMapper;

    public OrderEventKafkaListener(DeliveryApplicationService deliveryApplicationService,
                                   ObjectMapper objectMapper) {
        this.deliveryApplicationService = deliveryApplicationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-events", groupId = "delivery-service-group")
    public void receive(@Payload String message) {
        log.info("Received order event payload: {}", message);
        try {
            Map<String, Object> event = objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {});
            
            // Check status: only process if status is CREATED or similar
            String status = (String) event.get("status");
            if (status != null && !status.equalsIgnoreCase("CREATED") && !status.equalsIgnoreCase("ORDER_CREATED") && !status.equalsIgnoreCase("PENDING")) {
                log.info("Ignoring order event with status: {}", status);
                return;
            }

            String orderIdStr = (String) event.get("orderId");
            if (orderIdStr == null) {
                log.warn("Order event does not contain an orderId");
                return;
            }
            UUID orderId = UUID.fromString(orderIdStr);
            
            double pickupLat = event.get("pickupLatitude") != null ? Double.parseDouble(event.get("pickupLatitude").toString()) : 0.0;
            double pickupLon = event.get("pickupLongitude") != null ? Double.parseDouble(event.get("pickupLongitude").toString()) : 0.0;
            double dropoffLat = event.get("dropoffLatitude") != null ? Double.parseDouble(event.get("dropoffLatitude").toString()) : 0.0;
            double dropoffLon = event.get("dropoffLongitude") != null ? Double.parseDouble(event.get("dropoffLongitude").toString()) : 0.0;

            LocationDto pickup = new LocationDto(pickupLat, pickupLon);
            LocationDto dropoff = new LocationDto(dropoffLat, dropoffLon);

            double distanceKm = event.get("distanceKm") != null ? Double.parseDouble(event.get("distanceKm").toString()) : 5.0;
            BigDecimal deliveryFee = event.get("deliveryFee") != null ? new BigDecimal(event.get("deliveryFee").toString()) : new BigDecimal("3.50");
            BigDecimal tipAmount = event.get("tipAmount") != null ? new BigDecimal(event.get("tipAmount").toString()) : BigDecimal.ZERO;

            log.info("Triggering assignment search for order ID: {}", orderId);
            deliveryApplicationService.assignDriverForOrder(orderId, pickup, dropoff, distanceKm, deliveryFee, tipAmount);

        } catch (Exception e) {
            log.error("Failed to parse and process order event", e);
        }
    }
}
