package com.fooddelivery.payment.dataaccess.adapter;

import com.fooddelivery.common.domain.valueobject.CustomerId;
import com.fooddelivery.payment.dataaccess.entity.CreditEntryEntity;
import com.fooddelivery.payment.dataaccess.mapper.PaymentDataAccessMapper;
import com.fooddelivery.payment.dataaccess.repository.CreditEntryJpaRepository;
import com.fooddelivery.payment.domain.entity.CreditEntry;
import com.fooddelivery.payment.domain.port.output.repository.CreditEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CreditEntryRepositoryImpl implements CreditEntryRepository {

    private final CreditEntryJpaRepository creditEntryJpaRepository;
    private final PaymentDataAccessMapper paymentDataAccessMapper;

    @Override
    public CreditEntry save(CreditEntry creditEntry) {
        CreditEntryEntity entity = paymentDataAccessMapper.creditEntryToCreditEntryEntity(creditEntry);
        CreditEntryEntity saved = creditEntryJpaRepository.save(entity);
        return paymentDataAccessMapper.creditEntryEntityToCreditEntry(saved);
    }

    @Override
    public Optional<CreditEntry> findByCustomerId(CustomerId customerId) {
        return creditEntryJpaRepository.findByCustomerId(customerId.getValue())
                .map(paymentDataAccessMapper::creditEntryEntityToCreditEntry);
    }
}
