package com.fooddelivery.restaurant.messaging.publisher;

import com.fooddelivery.restaurant.domain.event.RestaurantMenuUpdatedEvent;
import com.fooddelivery.restaurant.domain.port.output.message.publisher.RestaurantMenuUpdatedMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantMenuUpdatedKafkaPublisher implements RestaurantMenuUpdatedMessagePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "restaurant-menu-updated";

    @Override
    public void publish(RestaurantMenuUpdatedEvent event) {
        String key = event.getAggregate().getId().getValue().toString();
        log.info("Publishing RestaurantMenuUpdatedEvent to topic {} with key {}", TOPIC, key);

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", event.getEventId());
        payload.put("restaurantId", key);
        payload.put("occurredOn", event.getOccurredOn().toString());

        kafkaTemplate.send(TOPIC, key, payload);
    }
}
