package com.fooddelivery.payment.domain.entity;

import com.fooddelivery.common.domain.entity.AggregateRoot;
import com.fooddelivery.common.domain.valueobject.*;
import com.fooddelivery.payment.domain.event.PaymentCompletedEvent;
import com.fooddelivery.payment.domain.event.PaymentFailedEvent;
import com.fooddelivery.payment.domain.event.PaymentRefundedEvent;
import com.fooddelivery.payment.domain.valueobject.PaymentId;
import com.fooddelivery.payment.domain.valueobject.PaymentMethod;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Slf4j
public class Payment extends AggregateRoot<PaymentId> {

    private final OrderId orderId;
    private final CustomerId customerId;
    private final Money price;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String gatewayTransactionId;
    private String gatewayProvider;
    private String failureMessage;
    private String idempotencyKey;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    private Payment(Builder builder) {
        super.setId(builder.paymentId);
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.price = builder.price;
        this.paymentStatus = builder.paymentStatus;
        this.paymentMethod = builder.paymentMethod;
        this.gatewayTransactionId = builder.gatewayTransactionId;
        this.gatewayProvider = builder.gatewayProvider;
        this.failureMessage = builder.failureMessage;
        this.idempotencyKey = builder.idempotencyKey;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public PaymentCompletedEvent initializePayment() {
        this.paymentStatus = PaymentStatus.PENDING;
        this.createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
        log.info("Payment initialized for orderId: {}", orderId.getValue());
        return new PaymentCompletedEvent(this, this.createdAt, new ArrayList<>());
    }

    public PaymentCompletedEvent completePayment(String gatewayTransactionId, String gatewayProvider) {
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.gatewayTransactionId = gatewayTransactionId;
        this.gatewayProvider = gatewayProvider;
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
        log.info("Payment completed for orderId: {} with transactionId: {}", orderId.getValue(), gatewayTransactionId);
        return new PaymentCompletedEvent(this, this.updatedAt, new ArrayList<>());
    }

    public PaymentFailedEvent failPayment(String reason) {
        this.paymentStatus = PaymentStatus.FAILED;
        this.failureMessage = reason;
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
        log.error("Payment failed for orderId: {} with reason: {}", orderId.getValue(), reason);
        return new PaymentFailedEvent(this, this.updatedAt, List.of(reason));
    }

    public PaymentRefundedEvent initiateRefund() {
        this.paymentStatus = PaymentStatus.REFUND_INITIATED;
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
        log.info("Refund initiated for orderId: {}", orderId.getValue());
        return new PaymentRefundedEvent(this, this.updatedAt, new ArrayList<>());
    }

    public PaymentRefundedEvent completeRefund() {
        this.paymentStatus = PaymentStatus.REFUNDED;
        this.updatedAt = ZonedDateTime.now(ZoneId.of("UTC"));
        log.info("Refund completed for orderId: {}", orderId.getValue());
        return new PaymentRefundedEvent(this, this.updatedAt, new ArrayList<>());
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public void setGatewayProvider(String gatewayProvider) {
        this.gatewayProvider = gatewayProvider;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private PaymentId paymentId;
        private OrderId orderId;
        private CustomerId customerId;
        private Money price;
        private PaymentStatus paymentStatus;
        private PaymentMethod paymentMethod;
        private String gatewayTransactionId;
        private String gatewayProvider;
        private String failureMessage;
        private String idempotencyKey;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        public Builder paymentId(PaymentId val) { paymentId = val; return this; }
        public Builder orderId(OrderId val) { orderId = val; return this; }
        public Builder customerId(CustomerId val) { customerId = val; return this; }
        public Builder price(Money val) { price = val; return this; }
        public Builder paymentStatus(PaymentStatus val) { paymentStatus = val; return this; }
        public Builder paymentMethod(PaymentMethod val) { paymentMethod = val; return this; }
        public Builder gatewayTransactionId(String val) { gatewayTransactionId = val; return this; }
        public Builder gatewayProvider(String val) { gatewayProvider = val; return this; }
        public Builder failureMessage(String val) { failureMessage = val; return this; }
        public Builder idempotencyKey(String val) { idempotencyKey = val; return this; }
        public Builder createdAt(ZonedDateTime val) { createdAt = val; return this; }
        public Builder updatedAt(ZonedDateTime val) { updatedAt = val; return this; }
        public Payment build() { return new Payment(this); }
    }
}
