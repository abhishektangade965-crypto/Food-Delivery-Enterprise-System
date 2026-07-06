package com.fooddelivery.payment.application.rest;

import com.fooddelivery.payment.application.dto.PaymentRequest;
import com.fooddelivery.payment.application.dto.PaymentResponse;
import com.fooddelivery.payment.application.service.PaymentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payments validation and processing")
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;

    @PostMapping("/process")
    @Operation(summary = "Validate and process payment for order")
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody @Valid PaymentRequest request) {
        log.info("Received request to process payment for order: {}", request.orderId());
        PaymentResponse response = paymentApplicationService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel and refund payment for order")
    @ApiResponse(responseCode = "200", description = "Payment cancelled successfully")
    public ResponseEntity<PaymentResponse> cancelPayment(@RequestBody @Valid PaymentRequest request) {
        log.info("Received request to cancel payment for order: {}", request.orderId());
        PaymentResponse response = paymentApplicationService.cancelPayment(request);
        return ResponseEntity.ok(response);
    }
}
