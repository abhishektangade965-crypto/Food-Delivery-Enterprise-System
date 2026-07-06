package com.fooddelivery.common.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing outbox messages.
 */
@Repository
public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {

    List<OutboxMessage> findByStatusAndAggregateType(OutboxStatus status, String aggregateType);

    List<OutboxMessage> findByStatus(OutboxStatus status);

    @Query("SELECT o FROM OutboxMessage o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<OutboxMessage> findByStatusOrderByCreatedAtAsc(@Param("status") OutboxStatus status);

    @Query("SELECT o FROM OutboxMessage o WHERE o.status = :status AND o.aggregateType = :aggregateType ORDER BY o.createdAt ASC")
    List<OutboxMessage> findByStatusAndAggregateTypeOrderByCreatedAt(
            @Param("status") OutboxStatus status,
            @Param("aggregateType") String aggregateType);

    Optional<OutboxMessage> findByIdAndStatus(UUID id, OutboxStatus status);

    @Modifying
    @Query("DELETE FROM OutboxMessage o WHERE o.status = :status AND o.createdAt < :cutoffTime")
    int deleteByStatusAndCreatedAtBefore(
            @Param("status") OutboxStatus status,
            @Param("cutoffTime") ZonedDateTime cutoffTime);

    @Query("SELECT COUNT(o) FROM OutboxMessage o WHERE o.status = :status")
    long countByStatus(@Param("status") OutboxStatus status);

    List<OutboxMessage> findBySagaId(UUID sagaId);

    @Query("SELECT o FROM OutboxMessage o WHERE o.status = 'STARTED' AND o.retryCount < :maxRetries ORDER BY o.createdAt ASC")
    List<OutboxMessage> findPendingForRetry(@Param("maxRetries") int maxRetries);
}
