package com.fooddelivery.common.infrastructure.inbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InboxRepository extends JpaRepository<InboxMessage, UUID> {
    Optional<InboxMessage> findByMessageIdAndConsumerGroup(String messageId, String consumerGroup);
}
