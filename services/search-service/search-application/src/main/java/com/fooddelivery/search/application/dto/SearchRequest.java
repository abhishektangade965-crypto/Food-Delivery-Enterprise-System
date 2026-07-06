package com.fooddelivery.search.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String query;
    private Double latitude;
    private Double longitude;
    private List<String> cuisines;
    private Boolean vegOnly;
    private String priceRange;
    private Double minRating;
    private String sortBy;
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 10;
}
