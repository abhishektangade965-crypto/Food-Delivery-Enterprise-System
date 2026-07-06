package com.fooddelivery.user.messaging.publisher;

import com.fooddelivery.user.domain.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(UserCreatedEvent event) {
        String topic = "user-events";
        String key = event.getUser().getId().getValue().toString();
        log.info("Publishing UserCreatedEvent to topic {} with key {}", topic, key);
        kafkaTemplate.send(topic, key, event.getUser().getEmail());
    }
}
