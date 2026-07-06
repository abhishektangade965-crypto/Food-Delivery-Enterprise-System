package com.fooddelivery.search.domain.port.output.repository;

import com.fooddelivery.search.domain.model.RestaurantSearchModel;
import com.fooddelivery.search.domain.model.RestaurantSearchResult;

import java.util.List;

public interface RestaurantSearchRepository {
    void save(RestaurantSearchModel restaurant);
    void deleteById(String id);
    
    RestaurantSearchResult search(
        String query,
        Double latitude,
        Double longitude,
        List<String> cuisines,
        Boolean vegOnly,
        String priceRange,
        Double minRating,
        String sortBy,
        int page,
        int size
    );

    List<String> autocomplete(String keyword);
    List<String> suggestions(String keyword);
}
