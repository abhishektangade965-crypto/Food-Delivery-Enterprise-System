package com.fooddelivery.common.infrastructure.outbox;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scheduled component that implements the Transactional Outbox Pattern.
 * Polls for pending outbox messages and publishes them to Kafka.
 * Uses optimistic locking to handle concurrent processing safely.
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OutboxScheduler {

    private static final int MAX_RETRY_COUNT = 5;
    private static final int BATCH_SIZE = 100;

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    /**
     * Main outbox polling task - runs every 2 seconds.
     * Publishes STARTED messages to Kafka.
     */
    @Scheduled(fixedDelayString = "${outbox.scheduler.fixed-delay:2000}")
    @Transactional
    public void processOutboxMessages() {
        List<OutboxMessage> pendingMessages = outboxRepository.findPendingForRetry(MAX_RETRY_COUNT);

        if (pendingMessages.isEmpty()) {
            return;
        }

        log.debug("Processing {} outbox messages", pendingMessages.size());
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<OutboxMessage> batch = pendingMessages.stream()
                .limit(BATCH_SIZE)
                .toList();

        for (OutboxMessage message : batch) {
            try {
                publishMessage(message);
                successCount.incrementAndGet();
            } catch (Exception e) {
                log.error("Failed to publish outbox message id={}, aggregateType={}, attempt={}: {}",
                        message.getId(), message.getAggregateType(), message.getRetryCount() + 1, e.getMessage(), e);
                handlePublishFailure(message);
                failureCount.incrementAndGet();
            }
        }

        if (successCount.get() > 0 || failureCount.get() > 0) {
            log.info("Outbox processing complete: success={}, failure={}",
                    successCount.get(), failureCount.get());
        }

        meterRegistry.counter("outbox.messages.processed", "status", "success")
                .increment(successCount.get());
        meterRegistry.counter("outbox.messages.processed", "status", "failure")
                .increment(failureCount.get());
    }

    /**
     * Cleanup task - runs daily at 2 AM to remove old completed/failed messages.
     */
    @Scheduled(cron = "${outbox.scheduler.cleanup-cron:0 0 2 * * *}")
    @Transactional
    public void cleanupProcessedMessages() {
        ZonedDateTime cutoffTime = ZonedDateTime.now().minusDays(7);
        int deletedCompleted = outboxRepository.deleteByStatusAndCreatedAtBefore(
                OutboxStatus.COMPLETED, cutoffTime);
        int deletedFailed = outboxRepository.deleteByStatusAndCreatedAtBefore(
                OutboxStatus.FAILED, cutoffTime);
        log.info("Outbox cleanup: deleted {} completed and {} failed messages older than 7 days",
                deletedCompleted, deletedFailed);
    }

    private void publishMessage(OutboxMessage message) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                message.getTopic(),
                message.getAggregateId().toString(),
                message.getPayload()
        );

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Async Kafka send failed for outbox message id={}: {}",
                        message.getId(), ex.getMessage());
            } else {
                log.debug("Outbox message id={} sent to topic={}, partition={}, offset={}",
                        message.getId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        // For synchronous confirmation during this scheduled cycle:
        try {
            SendResult<String, String> result = future.get();
            log.debug("Published outbox message id={} to {}/{}",
                    message.getId(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().offset());
            message.markCompleted();
            outboxRepository.save(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish message to Kafka", e);
        }
    }

    private void handlePublishFailure(OutboxMessage message) {
        message.incrementRetryCount();
        if (message.hasExceededMaxRetries(MAX_RETRY_COUNT)) {
            log.error("Outbox message id={} exceeded max retries ({}), marking as FAILED",
                    message.getId(), MAX_RETRY_COUNT);
            message.markFailed();
        }
        outboxRepository.save(message);
    }
}
