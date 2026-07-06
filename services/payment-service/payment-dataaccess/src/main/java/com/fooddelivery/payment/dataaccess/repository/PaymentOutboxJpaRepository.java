package com.fooddelivery.payment.dataaccess.repository;

import com.fooddelivery.payment.dataaccess.entity.PaymentOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentOutboxJpaRepository extends JpaRepository<PaymentOutboxEntity, UUID> {
    List<PaymentOutboxEntity> findByOutboxStatusAndType(String outboxStatus, String type);
    Optional<PaymentOutboxEntity> findBySagaIdAndType(UUID sagaId, String type);
}
