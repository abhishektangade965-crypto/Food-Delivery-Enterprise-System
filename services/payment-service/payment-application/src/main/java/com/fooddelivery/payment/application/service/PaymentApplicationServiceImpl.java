package com.fooddelivery.payment.application.service;

import com.fooddelivery.common.domain.valueobject.CustomerId;
import com.fooddelivery.common.domain.valueobject.Money;
import com.fooddelivery.common.domain.valueobject.OrderId;
import com.fooddelivery.common.domain.valueobject.PaymentStatus;
import com.fooddelivery.payment.application.dto.PaymentRequest;
import com.fooddelivery.payment.application.dto.PaymentResponse;
import com.fooddelivery.payment.domain.entity.CreditEntry;
import com.fooddelivery.payment.domain.entity.CreditHistory;
import com.fooddelivery.payment.domain.entity.Payment;
import com.fooddelivery.payment.domain.event.PaymentCompletedEvent;
import com.fooddelivery.payment.domain.event.PaymentFailedEvent;
import com.fooddelivery.payment.domain.event.PaymentRefundedEvent;
import com.fooddelivery.payment.domain.port.output.message.publisher.PaymentResponseMessagePublisher;
import com.fooddelivery.payment.domain.port.output.repository.CreditEntryRepository;
import com.fooddelivery.payment.domain.port.output.repository.CreditHistoryRepository;
import com.fooddelivery.payment.domain.port.output.repository.PaymentRepository;
import com.fooddelivery.payment.domain.service.PaymentDomainService;
import com.fooddelivery.payment.domain.service.PaymentDomainServiceImpl;
import com.fooddelivery.payment.domain.valueobject.PaymentId;
import com.fooddelivery.payment.domain.valueobject.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationServiceImpl implements PaymentApplicationService {

    private final PaymentDomainService paymentDomainService = new PaymentDomainServiceImpl();
    private final PaymentRepository paymentRepository;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final PaymentResponseMessagePublisher paymentResponseMessagePublisher;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment request for order id: {}", request.orderId());

        Payment payment = Payment.builder()
                .paymentId(new PaymentId(UUID.randomUUID()))
                .orderId(new OrderId(UUID.fromString(request.orderId())))
                .customerId(new CustomerId(UUID.fromString(request.customerId())))
                .price(new Money(new BigDecimal(request.price())))
                .paymentMethod(PaymentMethod.valueOf(request.paymentMethod()))
                .build();

        List<String> failureMessages = new ArrayList<>();
        CreditEntry creditEntry = null;
        List<CreditHistory> creditHistories = new ArrayList<>();

        if (payment.getPaymentMethod() == PaymentMethod.WALLET) {
            creditEntry = getCreditEntry(payment.getCustomerId());
            creditHistories = creditHistoryRepository.findByCustomerId(payment.getCustomerId());
        }

        PaymentCompletedEvent completedEvent = paymentDomainService.validateAndInitiatePayment(
                payment, creditEntry, creditHistories, failureMessages
        );

        paymentRepository.save(payment);

        if (failureMessages.isEmpty()) {
            if (payment.getPaymentMethod() == PaymentMethod.WALLET) {
                creditEntryRepository.save(creditEntry);
                creditHistories.forEach(creditHistoryRepository::save);
            }
            paymentResponseMessagePublisher.publish(completedEvent);
        } else {
            PaymentFailedEvent failedEvent = new PaymentFailedEvent(payment, ZonedDateTime.now(), failureMessages);
            paymentResponseMessagePublisher.publish(failedEvent);
        }

        return PaymentResponse.builder()
                .paymentId(payment.getId().getValue().toString())
                .orderId(payment.getOrderId().getValue().toString())
                .customerId(payment.getCustomerId().getValue().toString())
                .price(payment.getPrice().getAmount().doubleValue())
                .paymentStatus(payment.getPaymentStatus().name())
                .failureMessages(failureMessages)
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(PaymentRequest request) {
        log.info("Cancelling payment for order id: {}", request.orderId());

        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(new OrderId(UUID.fromString(request.orderId())));
        if (paymentOpt.isEmpty()) {
            log.error("Payment not found for order id: {}", request.orderId());
            throw new IllegalArgumentException("Payment not found for order id: " + request.orderId());
        }

        Payment payment = paymentOpt.get();
        List<String> failureMessages = new ArrayList<>();
        CreditEntry creditEntry = null;
        List<CreditHistory> creditHistories = new ArrayList<>();

        if (payment.getPaymentMethod() == PaymentMethod.WALLET) {
            creditEntry = getCreditEntry(payment.getCustomerId());
            creditHistories = creditHistoryRepository.findByCustomerId(payment.getCustomerId());
        }

        PaymentRefundedEvent refundedEvent = paymentDomainService.validateAndCancelPayment(
                payment, creditEntry, creditHistories, failureMessages
        );

        paymentRepository.save(payment);

        if (payment.getPaymentMethod() == PaymentMethod.WALLET) {
            creditEntryRepository.save(creditEntry);
            creditHistories.forEach(creditHistoryRepository::save);
        }

        paymentResponseMessagePublisher.publish(refundedEvent);

        return PaymentResponse.builder()
                .paymentId(payment.getId().getValue().toString())
                .orderId(payment.getOrderId().getValue().toString())
                .customerId(payment.getCustomerId().getValue().toString())
                .price(payment.getPrice().getAmount().doubleValue())
                .paymentStatus(payment.getPaymentStatus().name())
                .failureMessages(failureMessages)
                .build();
    }

    private CreditEntry getCreditEntry(CustomerId customerId) {
        return creditEntryRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    CreditEntry entry = CreditEntry.builder()
                            .customerId(customerId)
                            .totalCreditAmount(new Money(new BigDecimal("1000.00"))) // default credit for demo
                            .build();
                    entry.initializeCreditEntry();
                    return creditEntryRepository.save(entry);
                });
    }
}
