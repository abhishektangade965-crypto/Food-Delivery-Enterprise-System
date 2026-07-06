package com.fooddelivery.common.infrastructure.inbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InboxHelper {

    private final InboxRepository inboxRepository;

    @Transactional(readOnly = true)
    public boolean isAlreadyProcessed(String messageId, String consumerGroup) {
        Optional<InboxMessage> inboxMessage = inboxRepository.findByMessageIdAndConsumerGroup(messageId, consumerGroup);
        if (inboxMessage.isPresent()) {
            InboxStatus status = inboxMessage.get().getStatus();
            log.info("Message id: {} for consumer group: {} has status: {}", messageId, consumerGroup, status);
            return status == InboxStatus.PROCESSED;
        }
        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveMessage(String messageId, String consumerGroup) {
        Optional<InboxMessage> existing = inboxRepository.findByMessageIdAndConsumerGroup(messageId, consumerGroup);
        if (existing.isEmpty()) {
            InboxMessage inboxMessage = InboxMessage.builder()
                    .messageId(messageId)
                    .consumerGroup(consumerGroup)
                    .status(InboxStatus.RECEIVED)
                    .build();
            inboxRepository.save(inboxMessage);
            log.info("Saved inbox message with id: {} for consumer group: {}", messageId, consumerGroup);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessed(String messageId, String consumerGroup) {
        inboxRepository.findByMessageIdAndConsumerGroup(messageId, consumerGroup)
                .ifPresent(inboxMessage -> {
                    inboxMessage.setStatus(InboxStatus.PROCESSED);
                    inboxMessage.setProcessedAt(Instant.now());
                    inboxRepository.save(inboxMessage);
                    log.info("Marked message id: {} as PROCESSED for group: {}", messageId, consumerGroup);
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String messageId, String consumerGroup, String failureReason) {
        inboxRepository.findByMessageIdAndConsumerGroup(messageId, consumerGroup)
                .ifPresent(inboxMessage -> {
                    inboxMessage.setStatus(InboxStatus.FAILED);
                    inboxMessage.setFailureReason(failureReason);
                    inboxMessage.setProcessedAt(Instant.now());
                    inboxRepository.save(inboxMessage);
                    log.info("Marked message id: {} as FAILED for group: {} due to: {}", messageId, consumerGroup, failureReason);
                });
    }
}
