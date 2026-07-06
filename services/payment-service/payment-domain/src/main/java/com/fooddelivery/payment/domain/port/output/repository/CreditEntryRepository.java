package com.fooddelivery.payment.domain.port.output.repository;

import com.fooddelivery.common.domain.valueobject.CustomerId;
import com.fooddelivery.payment.domain.entity.CreditEntry;

import java.util.Optional;

public interface CreditEntryRepository {
    CreditEntry save(CreditEntry creditEntry);
    Optional<CreditEntry> findByCustomerId(CustomerId customerId);
}
