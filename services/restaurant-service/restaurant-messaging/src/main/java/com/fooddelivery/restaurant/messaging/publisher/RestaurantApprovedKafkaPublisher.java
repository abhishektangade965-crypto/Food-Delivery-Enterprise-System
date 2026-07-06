package com.fooddelivery.restaurant.messaging.publisher;

import com.fooddelivery.restaurant.domain.event.RestaurantApprovedEvent;
import com.fooddelivery.restaurant.domain.port.output.message.publisher.RestaurantApprovedMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApprovedKafkaPublisher implements RestaurantApprovedMessagePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "restaurant-approved";

    @Override
    public void publish(RestaurantApprovedEvent event) {
        String key = event.getAggregate().getId().getValue().toString();
        log.info("Publishing RestaurantApprovedEvent to topic {} with key {}", TOPIC, key);

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", event.getEventId());
        payload.put("restaurantId", key);
        payload.put("name", event.getAggregate().getName());
        payload.put("ownerId", event.getAggregate().getOwnerId().toString());
        payload.put("status", event.getAggregate().getStatus().name());
        payload.put("occurredOn", event.getOccurredOn().toString());

        kafkaTemplate.send(TOPIC, key, payload);
    }
}
