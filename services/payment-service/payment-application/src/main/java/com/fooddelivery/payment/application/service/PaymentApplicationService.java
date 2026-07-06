package com.fooddelivery.payment.application.service;

import com.fooddelivery.payment.application.dto.PaymentRequest;
import com.fooddelivery.payment.application.dto.PaymentResponse;

public interface PaymentApplicationService {
    PaymentResponse processPayment(PaymentRequest request);
    PaymentResponse cancelPayment(PaymentRequest request);
}
