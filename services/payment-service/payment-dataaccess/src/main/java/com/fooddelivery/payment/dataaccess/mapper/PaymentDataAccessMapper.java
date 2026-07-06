package com.fooddelivery.payment.dataaccess.mapper;

import com.fooddelivery.common.domain.valueobject.CustomerId;
import com.fooddelivery.common.domain.valueobject.Money;
import com.fooddelivery.common.domain.valueobject.OrderId;
import com.fooddelivery.payment.dataaccess.entity.CreditEntryEntity;
import com.fooddelivery.payment.dataaccess.entity.CreditHistoryEntity;
import com.fooddelivery.payment.dataaccess.entity.PaymentEntity;
import com.fooddelivery.payment.domain.entity.CreditEntry;
import com.fooddelivery.payment.domain.entity.CreditHistory;
import com.fooddelivery.payment.domain.entity.Payment;
import com.fooddelivery.payment.domain.valueobject.CreditEntryId;
import com.fooddelivery.payment.domain.valueobject.CreditHistoryId;
import com.fooddelivery.payment.domain.valueobject.PaymentId;
import org.springframework.stereotype.Component;

@Component
public class PaymentDataAccessMapper {

    public PaymentEntity paymentToPaymentEntity(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId().getValue())
                .customerId(payment.getCustomerId().getValue())
                .orderId(payment.getOrderId().getValue())
                .price(payment.getPrice().getAmount())
                .status(payment.getPaymentStatus())
                .paymentMethod(payment.getPaymentMethod())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    public Payment paymentEntityToPayment(PaymentEntity entity) {
        Payment payment = Payment.builder()
                .customerId(new CustomerId(entity.getCustomerId()))
                .orderId(new OrderId(entity.getOrderId()))
                .price(new Money(entity.getPrice()))
                .paymentMethod(entity.getPaymentMethod())
                .createdAt(entity.getCreatedAt())
                .build();
        payment.setId(new PaymentId(entity.getId()));
        payment.setPaymentStatus(entity.getStatus());
        return payment;
    }

    public CreditEntryEntity creditEntryToCreditEntryEntity(CreditEntry creditEntry) {
        return CreditEntryEntity.builder()
                .id(creditEntry.getId().getValue())
                .customerId(creditEntry.getCustomerId().getValue())
                .totalCreditAmount(creditEntry.getTotalCreditAmount().getAmount())
                .build();
    }

    public CreditEntry creditEntryEntityToCreditEntry(CreditEntryEntity entity) {
        CreditEntry entry = CreditEntry.builder()
                .customerId(new CustomerId(entity.getCustomerId()))
                .totalCreditAmount(new Money(entity.getTotalCreditAmount()))
                .build();
        entry.setId(new CreditEntryId(entity.getId()));
        return entry;
    }

    public CreditHistoryEntity creditHistoryToCreditHistoryEntity(CreditHistory creditHistory) {
        return CreditHistoryEntity.builder()
                .id(creditHistory.getId().getValue())
                .customerId(creditHistory.getCustomerId().getValue())
                .amount(creditHistory.getPrice().getAmount())
                .type(creditHistory.getTransactionType())
                .build();
    }

    public CreditHistory creditHistoryEntityToCreditHistory(CreditHistoryEntity entity) {
        return CreditHistory.builder()
                .creditHistoryId(new CreditHistoryId(entity.getId()))
                .customerId(new CustomerId(entity.getCustomerId()))
                .price(new Money(entity.getAmount()))
                .transactionType(entity.getType())
                .build();
    }
}
