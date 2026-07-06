package com.fooddelivery.payment.domain.service;

import com.fooddelivery.payment.domain.entity.CreditEntry;
import com.fooddelivery.payment.domain.entity.CreditHistory;
import com.fooddelivery.payment.domain.entity.Payment;
import com.fooddelivery.payment.domain.event.PaymentCompletedEvent;
import com.fooddelivery.payment.domain.event.PaymentFailedEvent;
import com.fooddelivery.payment.domain.event.PaymentRefundedEvent;

import java.util.List;

public interface PaymentDomainService {

    PaymentCompletedEvent validateAndInitiatePayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages);

    PaymentRefundedEvent validateAndCancelPayment(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages);
}
