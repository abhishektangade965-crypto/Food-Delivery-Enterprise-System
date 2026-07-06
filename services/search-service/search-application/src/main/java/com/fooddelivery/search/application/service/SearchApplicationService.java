package com.fooddelivery.search.application.service;

import com.fooddelivery.search.application.dto.AutocompleteResponse;
import com.fooddelivery.search.application.dto.SearchRequest;
import com.fooddelivery.search.application.dto.SearchResponse;

public interface SearchApplicationService {
    SearchResponse search(SearchRequest searchRequest);
    AutocompleteResponse autocomplete(String query);
    AutocompleteResponse suggestions(String query);
}
