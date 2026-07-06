package com.fooddelivery.payment.dataaccess.adapter;

import com.fooddelivery.common.domain.valueobject.CustomerId;
import com.fooddelivery.payment.dataaccess.entity.CreditHistoryEntity;
import com.fooddelivery.payment.dataaccess.mapper.PaymentDataAccessMapper;
import com.fooddelivery.payment.dataaccess.repository.CreditHistoryJpaRepository;
import com.fooddelivery.payment.domain.entity.CreditHistory;
import com.fooddelivery.payment.domain.port.output.repository.CreditHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CreditHistoryRepositoryImpl implements CreditHistoryRepository {

    private final CreditHistoryJpaRepository creditHistoryJpaRepository;
    private final PaymentDataAccessMapper paymentDataAccessMapper;

    @Override
    public CreditHistory save(CreditHistory creditHistory) {
        CreditHistoryEntity entity = paymentDataAccessMapper.creditHistoryToCreditHistoryEntity(creditHistory);
        CreditHistoryEntity saved = creditHistoryJpaRepository.save(entity);
        return paymentDataAccessMapper.creditHistoryEntityToCreditHistory(saved);
    }

    @Override
    public List<CreditHistory> findByCustomerId(CustomerId customerId) {
        return creditHistoryJpaRepository.findByCustomerId(customerId.getValue()).stream()
                .map(paymentDataAccessMapper::creditHistoryEntityToCreditHistory)
                .collect(Collectors.toList());
    }
}
