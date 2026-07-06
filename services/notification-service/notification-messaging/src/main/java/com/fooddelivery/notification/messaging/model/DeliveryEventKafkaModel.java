package com.fooddelivery.notification.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEventKafkaModel {
    private UUID deliveryId;
    private UUID orderId;
    private UUID customerId;
    private String customerPhone;
    private String driverName;
    private String status;
}
