package com.fooddelivery.payment.application.dto;

import com.fooddelivery.common.domain.valueobject.PaymentStatus;
import com.fooddelivery.payment.domain.valueobject.PaymentMethod;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Builder
@Jacksonized
public class PaymentResponse {
    private UUID paymentId;
    private UUID orderId;
    private UUID customerId;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String gatewayTransactionId;
    private String gatewayProvider;
    private String clientSecret; // For Stripe frontend confirmation
    private String failureMessage;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String nextAction; // e.g., redirect URL for 3DS
}
