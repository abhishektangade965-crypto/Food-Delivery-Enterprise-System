package com.fooddelivery.search.application.rest;

import com.fooddelivery.search.application.dto.AutocompleteResponse;
import com.fooddelivery.search.application.dto.SearchRequest;
import com.fooddelivery.search.application.dto.SearchResponse;
import com.fooddelivery.search.application.service.SearchApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchApplicationService searchApplicationService;

    @GetMapping
    public ResponseEntity<SearchResponse> search(SearchRequest request) {
        SearchResponse response = searchApplicationService.search(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<AutocompleteResponse> autocomplete(@RequestParam("q") String query) {
        AutocompleteResponse response = searchApplicationService.autocomplete(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<AutocompleteResponse> suggestions(@RequestParam("q") String query) {
        AutocompleteResponse response = searchApplicationService.suggestions(query);
        return ResponseEntity.ok(response);
    }
}
