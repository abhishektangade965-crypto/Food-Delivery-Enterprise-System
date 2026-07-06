package com.fooddelivery.order.domain.exception;

import com.fooddelivery.common.domain.exception.DomainNotFoundException;

public class OrderNotFoundException extends DomainNotFoundException {
    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
