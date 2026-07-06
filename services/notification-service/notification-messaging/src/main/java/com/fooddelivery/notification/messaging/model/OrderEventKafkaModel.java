package com.fooddelivery.notification.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventKafkaModel {
    private UUID orderId;
    private UUID customerId;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal amount;
    private String status;
}
