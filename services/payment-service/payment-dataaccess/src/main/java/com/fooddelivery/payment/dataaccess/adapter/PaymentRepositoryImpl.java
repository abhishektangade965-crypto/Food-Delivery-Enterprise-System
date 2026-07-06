package com.fooddelivery.payment.dataaccess.adapter;

import com.fooddelivery.common.domain.valueobject.OrderId;
import com.fooddelivery.payment.dataaccess.entity.PaymentEntity;
import com.fooddelivery.payment.dataaccess.mapper.PaymentDataAccessMapper;
import com.fooddelivery.payment.dataaccess.repository.PaymentJpaRepository;
import com.fooddelivery.payment.domain.entity.Payment;
import com.fooddelivery.payment.domain.port.output.repository.PaymentRepository;
import com.fooddelivery.payment.domain.valueobject.PaymentId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentDataAccessMapper paymentDataAccessMapper;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = paymentDataAccessMapper.paymentToPaymentEntity(payment);
        PaymentEntity saved = paymentJpaRepository.save(entity);
        return paymentDataAccessMapper.paymentEntityToPayment(saved);
    }

    @Override
    public Optional<Payment> findById(PaymentId paymentId) {
        return paymentJpaRepository.findById(paymentId.getValue())
                .map(paymentDataAccessMapper::paymentEntityToPayment);
    }

    @Override
    public Optional<Payment> findByOrderId(OrderId orderId) {
        return paymentJpaRepository.findByOrderId(orderId.getValue())
                .map(paymentDataAccessMapper::paymentEntityToPayment);
    }
}
