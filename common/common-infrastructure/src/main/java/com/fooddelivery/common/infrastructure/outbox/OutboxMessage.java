package com.fooddelivery.common.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * JPA entity representing a message in the transactional outbox.
 * Used to ensure reliable message delivery via the Outbox Pattern.
 * Messages are written within the same transaction as domain changes,
 * then asynchronously published to the message broker.
 */
@Entity
@Table(name = "outbox_messages",
        indexes = {
            @Index(name = "idx_outbox_status_type", columnList = "status, aggregate_type"),
            @Index(name = "idx_outbox_created_at", columnList = "created_at"),
            @Index(name = "idx_outbox_aggregate_id", columnList = "aggregate_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxMessage {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "event_type", nullable = false, length = 150)
    private String eventType;

    @Column(name = "topic", nullable = false, length = 200)
    private String topic;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "saga_id")
    private UUID sagaId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "processed_at")
    private ZonedDateTime processedAt;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;

    @Version
    @Column(name = "version")
    private int version;

    @PrePersist
    protected void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
        if (status == null) {
            status = OutboxStatus.STARTED;
        }
    }

    public void markCompleted() {
        this.status = OutboxStatus.COMPLETED;
        this.processedAt = ZonedDateTime.now();
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
        this.processedAt = ZonedDateTime.now();
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean hasExceededMaxRetries(int maxRetries) {
        return this.retryCount >= maxRetries;
    }
}
