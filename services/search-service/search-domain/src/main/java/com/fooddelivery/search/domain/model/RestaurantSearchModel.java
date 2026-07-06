package com.fooddelivery.search.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSearchModel {
    private UUID id;
    private String name;
    private String description;
    private List<String> cuisineTypes;
    private List<String> tags;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private String priceRange;
    private Integer avgDeliveryTime;
    private Boolean isOpen;
    private Boolean isVeg;
    private BigDecimal deliveryFee;
    private Integer totalOrders;
    private Boolean featured;
}
