package com.purva.searchengine;

import com.purva.searchengine.index.InvertedIndex;
import com.purva.searchengine.service.SearchService;
import com.purva.searchengine.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchServiceTest {
    @Test
    void shouldReturnCorrectSearchResults() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();

        var tokens = tokenizer.tokenize("java programming language");
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize("python programming language");
        invertedIndex.index(2, tokens);

        tokens = tokenizer.tokenize("java python programming");
        invertedIndex.index(3, tokens);

        var searchService = new SearchService(tokenizer, invertedIndex);

        var results = searchService.search("java programming language");
        assertEquals(List.of(1), results);

        results = searchService.search("java programming");
        assertEquals(List.of(1, 3), results);

        results = searchService.search("python programming");
        assertEquals(List.of(2, 3), results);

        results = searchService.search("programming");
        assertEquals(List.of(1, 2, 3), results);

    }

    @Test
    void shouldHandleSearchWithStopWords() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();

        var tokens = tokenizer.tokenize("the quick brown fox");
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize("the lazy dog");
        invertedIndex.index(2, tokens);

        var searchService = new SearchService(tokenizer, invertedIndex);

        var results = searchService.search("the quick");
        assertEquals(List.of(1), results);

        results = searchService.search("the lazy");
        assertEquals(List.of(2), results);

        results = searchService.search("the");
        assertTrue(results.isEmpty(), "Should be empty as 'the' is a stop word and should be filtered out");
    }

    @Test
    void shouldHandleSearchWithSingleLetterTokens() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();

        var tokens = tokenizer.tokenize("a quick brown fox");
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize("the lazy dog");
        invertedIndex.index(2, tokens);

        var searchService = new SearchService(tokenizer, invertedIndex);

        var results = searchService.search("a quick");
        assertEquals(List.of(1), results);

        results = searchService.search("the lazy");
        assertEquals(List.of(2), results);

        results = searchService.search("a");
        assertTrue(results.isEmpty(), "Should be empty as 'a' is a single letter token and should be filtered out");
    }

    @Test
    void shouldHandleCaseInsensitiveSearch() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();

        var tokens = tokenizer.tokenize("java programming language");
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize("python programming language");
        invertedIndex.index(2, tokens);

        var searchService = new SearchService(tokenizer, invertedIndex);

        var results = searchService.search("java programming language");
        assertEquals(List.of(1), results);

        results = searchService.search("JAVA PROGRAMMING LANGUAGE");
        assertEquals(List.of(1), results);

        results = searchService.search("JaVa PrOgRaMmInG LaNgUaGe");
        assertEquals(List.of(1), results);
    }

    @Test
    void shouldReturnEmptyResultsForNonExistentTokens() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();

        var tokens = tokenizer.tokenize("java programming language");
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize("python programming language");
        invertedIndex.index(2, tokens);

        var searchService = new SearchService(tokenizer, invertedIndex);

        var results = searchService.search("ruby programming");
        assertTrue(results.isEmpty());

        results = searchService.search("javascript programming");
        assertTrue(results.isEmpty());
    }

    @Test
    void shouldReturnEmptyResultsForEmptyOrNullQuery() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var searchService = new SearchService(tokenizer, invertedIndex);

        var results = searchService.search("");
        assertTrue(results.isEmpty());

        results = searchService.search(null);
        assertTrue(results.isEmpty());
    }

    @Test
    void shouldReturnEmptyResultsWhenNoDocumentsMatchAllTokens() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();

        var tokens = tokenizer.tokenize("java programming");
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize("python programming");
        invertedIndex.index(2, tokens);

        tokens = tokenizer.tokenize("java python");
        invertedIndex.index(3, tokens);

        var searchService = new SearchService(tokenizer, invertedIndex);

        var results = searchService.search("java programming language");
        assertTrue(results.isEmpty(), "Should be empty as no document contains all tokens");
    }

    @Test
    void shouldReturnSortedResults() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();

        var tokens = tokenizer.tokenize("java programming");
        invertedIndex.index(3, tokens);

        tokens = tokenizer.tokenize("java programming");
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize("java programming");
        invertedIndex.index(2, tokens);

        var searchService = new SearchService(tokenizer, invertedIndex);

        var results = searchService.search("java programming");
        assertEquals(List.of(1, 2, 3), results, "Results should be sorted by document ID");
    }
}
