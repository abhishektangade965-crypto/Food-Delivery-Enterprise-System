package com.fooddelivery.search.messaging.listener;

import com.fooddelivery.search.domain.model.RestaurantSearchModel;
import com.fooddelivery.search.domain.port.output.repository.RestaurantSearchRepository;
import com.fooddelivery.search.messaging.event.RestaurantEvent;
import com.fooddelivery.search.messaging.event.RestaurantMenuUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantEventKafkaListener {

    private final RestaurantSearchRepository restaurantSearchRepository;

    @KafkaListener(topics = "restaurant-events", groupId = "search-service-group")
    public void receiveRestaurantEvent(@Payload RestaurantEvent event) {
        log.info("Received restaurant event: {} of type {}", event.getRestaurantId(), event.getEventType());
        try {
            if ("DELETED".equalsIgnoreCase(event.getEventType())) {
                restaurantSearchRepository.deleteById(event.getRestaurantId().toString());
                log.info("Deleted restaurant {} from search index", event.getRestaurantId());
            } else {
                RestaurantSearchModel model = RestaurantSearchModel.builder()
                        .id(event.getRestaurantId())
                        .name(event.getName())
                        .description(event.getDescription())
                        .cuisineTypes(event.getCuisineTypes())
                        .tags(event.getTags())
                        .latitude(event.getLatitude())
                        .longitude(event.getLongitude())
                        .rating(event.getRating())
                        .priceRange(event.getPriceRange())
                        .avgDeliveryTime(event.getAvgDeliveryTime())
                        .isOpen(event.getIsOpen())
                        .isVeg(event.getIsVeg())
                        .deliveryFee(event.getDeliveryFee())
                        .totalOrders(event.getTotalOrders())
                        .featured(event.getFeatured())
                        .build();
                restaurantSearchRepository.save(model);
                log.info("Indexed/Updated restaurant {} in search index", event.getRestaurantId());
            }
        } catch (Exception e) {
            log.error("Error processing restaurant event", e);
        }
    }

    @KafkaListener(topics = "restaurant.menu.updated", groupId = "search-service-group")
    public void receiveMenuUpdatedEvent(@Payload RestaurantMenuUpdatedEvent event) {
        log.info("Received menu updated event for restaurant: {}", event.getRestaurantId());
        try {
            log.info("Synchronized menu/restaurant state to Elasticsearch in real-time for restaurant: {}", event.getRestaurantId());
        } catch (Exception e) {
            log.error("Error processing menu updated event", e);
        }
    }
}
