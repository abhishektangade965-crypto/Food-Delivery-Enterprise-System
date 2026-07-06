package com.fooddelivery.payment.application.dto;

import com.fooddelivery.payment.domain.valueobject.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@Jacksonized
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String paymentMethodToken; // Stripe payment method token for card payments

    private String currency; // e.g., "INR", "USD"

    private String idempotencyKey;

    private String description;

    private String returnUrl; // For 3D Secure redirects

    private boolean savePaymentMethod;
}
