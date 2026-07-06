package com.fooddelivery.search.application.service;

import com.fooddelivery.search.application.dto.AutocompleteResponse;
import com.fooddelivery.search.application.dto.FacetCount;
import com.fooddelivery.search.application.dto.SearchRequest;
import com.fooddelivery.search.application.dto.SearchResponse;
import com.fooddelivery.search.domain.model.RestaurantSearchResult;
import com.fooddelivery.search.domain.port.output.repository.RestaurantSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchApplicationServiceImpl implements SearchApplicationService {

    private final RestaurantSearchRepository restaurantSearchRepository;

    @Override
    public SearchResponse search(SearchRequest request) {
        log.info("Searching restaurants with request: {}", request);
        
        RestaurantSearchResult result = restaurantSearchRepository.search(
            request.getQuery(),
            request.getLatitude(),
            request.getLongitude(),
            request.getCuisines(),
            request.getVegOnly(),
            request.getPriceRange(),
            request.getMinRating(),
            request.getSortBy(),
            request.getPage(),
            request.getSize()
        );

        List<FacetCount> cuisineFacets = result.getCuisineFacets() == null ? Collections.emptyList() :
            result.getCuisineFacets().entrySet().stream()
                .map(entry -> FacetCount.builder().term(entry.getKey()).count(entry.getValue()).build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());

        List<FacetCount> priceRangeFacets = result.getPriceRangeFacets() == null ? Collections.emptyList() :
            result.getPriceRangeFacets().entrySet().stream()
                .map(entry -> FacetCount.builder().term(entry.getKey()).count(entry.getValue()).build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());

        return SearchResponse.builder()
            .restaurants(result.getRestaurants())
            .totalHits(result.getTotalHits())
            .cuisineFacets(cuisineFacets)
            .priceRangeFacets(priceRangeFacets)
            .page(request.getPage())
            .size(request.getSize())
            .build();
    }

    @Override
    public AutocompleteResponse autocomplete(String query) {
        log.info("Fetching autocomplete suggestions for query: {}", query);
        List<String> suggestions = restaurantSearchRepository.autocomplete(query);
        return AutocompleteResponse.builder()
            .suggestions(suggestions)
            .build();
    }

    @Override
    public AutocompleteResponse suggestions(String query) {
        log.info("Fetching suggestions/spell-corrections for query: {}", query);
        List<String> suggestions = restaurantSearchRepository.suggestions(query);
        return AutocompleteResponse.builder()
            .suggestions(suggestions)
            .build();
    }
}
