package com.purva.searchengine.controller;

import com.purva.searchengine.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(name = "query") String query, @RequestParam(name = "topK", required = false) Integer topK) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query parameter cannot be empty");
        }
        List<?> searchResult;
        if (topK == null) {
            searchResult = searchService.search(query);
        } else {
            if (topK <= 0) {
                throw new IllegalArgumentException("topK must be greater than 0");
            }
            searchResult = searchService.rankedSearch(query, topK);
        }
        return ResponseEntity.ok(searchResult);
    }
}
