package com.fooddelivery.delivery.messaging.publisher;

import com.fooddelivery.delivery.domain.entity.DeliveryAssignment;
import com.fooddelivery.delivery.domain.port.output.message.publisher.DeliveryEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DeliveryEventKafkaPublisher implements DeliveryEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "delivery-events";

    public DeliveryEventKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DeliveryAssignment deliveryAssignment) {
        if (deliveryAssignment == null || deliveryAssignment.getId() == null) {
            log.warn("Cannot publish null delivery assignment event");
            return;
        }

        String key = deliveryAssignment.getId().getValue().toString();
        log.info("Publishing DeliveryEvent to topic {} for assignment: {}", TOPIC, key);

        Map<String, Object> payload = new HashMap<>();
        payload.put("assignmentId", key);
        payload.put("orderId", deliveryAssignment.getOrderId() != null ? deliveryAssignment.getOrderId().getValue().toString() : null);
        payload.put("driverId", deliveryAssignment.getDriverId() != null ? deliveryAssignment.getDriverId().getValue().toString() : null);
        payload.put("status", deliveryAssignment.getStatus() != null ? deliveryAssignment.getStatus().name() : null);
        payload.put("deliveryFee", deliveryAssignment.getDeliveryFee() != null ? deliveryAssignment.getDeliveryFee().toString() : "0.00");
        payload.put("tipAmount", deliveryAssignment.getTipAmount() != null ? deliveryAssignment.getTipAmount().toString() : "0.00");
        payload.put("otp", deliveryAssignment.getOtp());
        payload.put("otpVerified", deliveryAssignment.getOtpVerified());
        payload.put("estimatedPickupTime", deliveryAssignment.getEstimatedPickupTime() != null ? deliveryAssignment.getEstimatedPickupTime().toString() : null);
        payload.put("estimatedDeliveryTime", deliveryAssignment.getEstimatedDeliveryTime() != null ? deliveryAssignment.getEstimatedDeliveryTime().toString() : null);
        payload.put("actualPickupTime", deliveryAssignment.getActualPickupTime() != null ? deliveryAssignment.getActualPickupTime().toString() : null);
        payload.put("actualDeliveryTime", deliveryAssignment.getActualDeliveryTime() != null ? deliveryAssignment.getActualDeliveryTime().toString() : null);

        kafkaTemplate.send(TOPIC, key, payload);
    }
}
