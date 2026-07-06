package com.fooddelivery.payment.domain.port.output.repository;

import com.fooddelivery.common.domain.valueobject.CustomerId;
import com.fooddelivery.payment.domain.entity.CreditHistory;

import java.util.List;

public interface CreditHistoryRepository {
    CreditHistory save(CreditHistory creditHistory);
    List<CreditHistory> findByCustomerId(CustomerId customerId);
}
