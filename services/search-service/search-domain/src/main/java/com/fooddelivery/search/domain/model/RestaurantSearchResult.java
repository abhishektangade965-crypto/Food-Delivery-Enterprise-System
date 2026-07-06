package com.fooddelivery.search.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSearchResult {
    private List<RestaurantSearchModel> restaurants;
    private long totalHits;
    private Map<String, Long> cuisineFacets;
    private Map<String, Long> priceRangeFacets;
}
