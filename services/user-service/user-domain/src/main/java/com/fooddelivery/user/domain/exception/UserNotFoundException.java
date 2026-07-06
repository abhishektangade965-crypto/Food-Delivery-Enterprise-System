package com.fooddelivery.user.domain.exception;

import com.fooddelivery.common.domain.exception.DomainNotFoundException;

public class UserNotFoundException extends DomainNotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
