package com.fooddelivery.search.application.dto;

import com.fooddelivery.search.domain.model.RestaurantSearchModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<RestaurantSearchModel> restaurants;
    private long totalHits;
    private List<FacetCount> cuisineFacets;
    private List<FacetCount> priceRangeFacets;
    private Integer page;
    private Integer size;
}
