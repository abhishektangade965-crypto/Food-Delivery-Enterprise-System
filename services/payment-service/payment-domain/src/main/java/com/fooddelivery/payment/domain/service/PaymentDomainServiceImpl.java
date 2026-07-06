package com.fooddelivery.payment.domain.service;

import com.fooddelivery.common.domain.valueobject.Money;
import com.fooddelivery.common.domain.valueobject.PaymentStatus;
import com.fooddelivery.payment.domain.entity.CreditEntry;
import com.fooddelivery.payment.domain.entity.CreditHistory;
import com.fooddelivery.payment.domain.entity.Payment;
import com.fooddelivery.payment.domain.event.PaymentCompletedEvent;
import com.fooddelivery.payment.domain.event.PaymentFailedEvent;
import com.fooddelivery.payment.domain.event.PaymentRefundedEvent;
import com.fooddelivery.payment.domain.valueobject.CreditHistoryId;
import com.fooddelivery.payment.domain.valueobject.TransactionType;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
public class PaymentDomainServiceImpl implements PaymentDomainService {

    @Override
    public PaymentCompletedEvent validateAndInitiatePayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages) {

        payment.initializePayment();
        validatePaymentPrice(payment, failureMessages);

        if (payment.getPaymentMethod() == com.fooddelivery.payment.domain.valueobject.PaymentMethod.WALLET) {
            validateCreditEntry(payment, creditEntry, failureMessages);
            subtractCreditEntry(payment, creditEntry);
            updateCreditHistory(payment, creditHistories, TransactionType.DEBIT);
            validateCreditHistory(creditEntry, creditHistories, failureMessages);
        }

        if (failureMessages.isEmpty()) {
            log.info("Payment is initiated for order id: {}", payment.getOrderId().getValue());
            return payment.completePayment(null, null);
        } else {
            log.error("Customer with id: {} does not have sufficient credit for order id: {}",
                    payment.getCustomerId().getValue(), payment.getOrderId().getValue());
            return new PaymentCompletedEvent(payment, ZonedDateTime.now(ZoneId.of("UTC")), failureMessages);
        }
    }

    @Override
    public PaymentRefundedEvent validateAndCancelPayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages) {

        payment.initiateRefund();
        addCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistories, TransactionType.CREDIT);
        log.info("Payment is cancelled for order id: {}", payment.getOrderId().getValue());
        return payment.completeRefund();
    }

    private void validatePaymentPrice(Payment payment, List<String> failureMessages) {
        if (payment.getPrice() == null || !payment.getPrice().isPositive()) {
            failureMessages.add("Total price must be greater than zero!");
        }
    }

    private void validateCreditEntry(Payment payment, CreditEntry creditEntry, List<String> failureMessages) {
        if (payment.getPrice().isGreaterThan(creditEntry.getTotalCreditAmount())) {
            log.error("Customer with id: {} does not have enough credit for order id: {}",
                    payment.getCustomerId().getValue(), payment.getOrderId().getValue());
            failureMessages.add("Customer with id=" + payment.getCustomerId().getValue()
                    + " does not have enough credit according to payment service!");
        }
    }

    private void subtractCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.subtractCreditAmount(payment.getPrice());
    }

    private void addCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.addCreditAmount(payment.getPrice());
    }

    private void updateCreditHistory(Payment payment, List<CreditHistory> creditHistories, TransactionType transactionType) {
        creditHistories.add(CreditHistory.builder()
                .creditHistoryId(new CreditHistoryId(UUID.randomUUID()))
                .customerId(payment.getCustomerId())
                .price(payment.getPrice())
                .transactionType(transactionType)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build());
    }

    private void validateCreditHistory(CreditEntry creditEntry, List<CreditHistory> creditHistories, List<String> failureMessages) {
        Money totalCreditHistory = creditHistories.stream()
                .filter(creditHistory -> TransactionType.CREDIT == creditHistory.getTransactionType())
                .map(CreditHistory::getPrice)
                .reduce(Money.ZERO, Money::add);

        Money totalDebitHistory = creditHistories.stream()
                .filter(creditHistory -> TransactionType.DEBIT == creditHistory.getTransactionType())
                .map(CreditHistory::getPrice)
                .reduce(Money.ZERO, Money::add);

        if (totalDebitHistory.isGreaterThan(totalCreditHistory)) {
            log.error("Customer balance is: {}", creditEntry.getTotalCreditAmount().getAmount());
            failureMessages.add("Customer with id=" + creditEntry.getCustomerId().getValue()
                    + " does not have enough credit according to credit history!");
        }
    }
}
