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
    public ResponseEntity<?> search(@RequestParam(name = "query") String query, @RequestParam(name = "topK", required = false) Integer topK, @RequestParam(name = "threshold", required = false, defaultValue = "0.8") Double threshold) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query parameter cannot be empty");
        }
        if (threshold <= 0 || threshold > 1) {
            throw new IllegalArgumentException("Threshold must be in the range (0, 1]");
        }
        List<?> searchResult;
        if (topK == null) {
            searchResult = searchService.search(query, threshold);
        } else {
            if (topK <= 0) {
                throw new IllegalArgumentException("topK must be greater than 0");
            }
            searchResult = searchService.rankedSearch(query, topK, threshold);
        }
        return ResponseEntity.ok(searchResult);
    }
}
