package com.purva.searchengine.search;

import com.purva.searchengine.index.InvertedIndex;
import com.purva.searchengine.service.SearchService;
import com.purva.searchengine.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchServiceTest {
    @Test
    void shouldReturnCorrectSearchResults() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var scorer = new Bm25Scorer(invertedIndex);

        var tokens = tokenizer.tokenize("java programming language");
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize("python programming language");
        invertedIndex.index(2, tokens);

        tokens = tokenizer.tokenize("java python programming");
        invertedIndex.index(3, tokens);

        var searchService = new SearchService(tokenizer, invertedIndex, scorer);

        var results = searchService.search("java programming language", 1.0);
        assertEquals(List.of(1), results);

        results = searchService.search("java programming", 1.0);
        assertEquals(List.of(1, 3), results);

        results = searchService.search("python programming", 1.0);
        assertEquals(List.of(2, 3), results);

        results = searchService.search("programming", 1.0);
        assertEquals(List.of(1, 2, 3), results);

    }

    @Test
    void shouldHandleSearchWithStopWords() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var scorer = new Bm25Scorer(invertedIndex);

        var tokens = tokenizer.tokenize("the quick brown fox");
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize("the lazy dog");
        invertedIndex.index(2, tokens);

        var searchService = new SearchService(tokenizer, invertedIndex, scorer);

        var results = searchService.search("the quick", 1.0);
        assertEquals(List.of(1), results);

        results = searchService.search("the lazy", 1.0);
        assertEquals(List.of(2), results);

        results = searchService.search("the", 1.0);
        assertTrue(results.isEmpty(), "Should be empty as 'the' is a stop word and should be filtered out");
    }

    @Test
    void shouldHandleSearchWithSingleLetterTokens() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var scorer = new Bm25Scorer(invertedIndex);

        var tokens = tokenizer.tokenize("a quick brown fox");
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize("the lazy dog");
        invertedIndex.index(2, tokens);

        var searchService = new SearchService(tokenizer, invertedIndex, scorer);

        var results = searchService.search("a quick", 1.0);
        assertEquals(List.of(1), results);

        results = searchService.search("the lazy", 1.0);
        assertEquals(List.of(2), results);

        results = searchService.search("a", 1.0);
        assertTrue(results.isEmpty(), "Should be empty as 'a' is a single letter token and should be filtered out");
    }

    @Test
    void shouldHandleCaseInsensitiveSearch() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var scorer = new Bm25Scorer(invertedIndex);

        var tokens = tokenizer.tokenize("java programming language");
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize("python programming language");
        invertedIndex.index(2, tokens);

        var searchService = new SearchService(tokenizer, invertedIndex, scorer);

        var results = searchService.search("java programming language", 1.0);
        assertEquals(List.of(1), results);

        results = searchService.search("JAVA PROGRAMMING LANGUAGE", 1.0);
        assertEquals(List.of(1), results);

        results = searchService.search("JaVa PrOgRaMmInG LaNgUaGe", 1.0);
        assertEquals(List.of(1), results);
    }

    @Test
    void shouldReturnEmptyResultsForNonExistentTokens() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var scorer = new Bm25Scorer(invertedIndex);

        var tokens = tokenizer.tokenize("java programming language");
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize("python programming language");
        invertedIndex.index(2, tokens);

        var searchService = new SearchService(tokenizer, invertedIndex, scorer);

        var results = searchService.search("ruby programming", 1.0);
        assertTrue(results.isEmpty());

        results = searchService.search("javascript programming", 1.0);
        assertTrue(results.isEmpty());
    }

    @Test
    void shouldReturnEmptyResultsForEmptyOrNullQuery() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var scorer = new Bm25Scorer(invertedIndex);
        var searchService = new SearchService(tokenizer, invertedIndex, scorer);

        var results = searchService.search("", 1.0);
        assertTrue(results.isEmpty());

        results = searchService.search(null, 1.0);
        assertTrue(results.isEmpty());
    }

    @Test
    void shouldReturnEmptyResultsWhenNoDocumentsMatchAllTokens() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var scorer = new Bm25Scorer(invertedIndex);

        var tokens = tokenizer.tokenize("java programming");
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize("python programming");
        invertedIndex.index(2, tokens);

        tokens = tokenizer.tokenize("java python");
        invertedIndex.index(3, tokens);

        var searchService = new SearchService(tokenizer, invertedIndex, scorer);

        var results = searchService.search("java programming language", 1.0);
        assertTrue(results.isEmpty(), "Should be empty as no document contains all tokens");
    }

    @Test
    void shouldReturnSortedResults() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var scorer = new Bm25Scorer(invertedIndex);

        var tokens = tokenizer.tokenize("java programming");
        invertedIndex.index(3, tokens);

        tokens = tokenizer.tokenize("java programming");
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize("java programming");
        invertedIndex.index(2, tokens);

        var searchService = new SearchService(tokenizer, invertedIndex, scorer);

        var results = searchService.search("java programming", 1.0);
        assertEquals(List.of(1, 2, 3), results, "Results should be sorted by document ID");
    }

    @Test
    void shouldThrowExceptionForInvalidThreshold() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var scorer = new Bm25Scorer(invertedIndex);
        var searchService = new SearchService(tokenizer, invertedIndex, scorer);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> searchService.search("java", -0.5),
                "Should throw when threshold is below 0"
        );

        assertEquals("Threshold must be in the range (0, 1]", exception.getMessage());

        exception = assertThrows(
                IllegalArgumentException.class,
                () -> searchService.search("java", 1.5),
                "Should throw when threshold is above 1"
        );

        assertEquals("Threshold must be in the range (0, 1]", exception.getMessage());

        exception = assertThrows(
                IllegalArgumentException.class,
                () -> searchService.search("java", 0),
                "Should throw when threshold is 0"
        );

        assertEquals("Threshold must be in the range (0, 1]", exception.getMessage());
    }

    @Test
    void shouldReturnResultsWhenThresholdIsMet() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var scorer = new Bm25Scorer(invertedIndex);

        invertedIndex.index(1, tokenizer.tokenize("java spring boot"));
        invertedIndex.index(2, tokenizer.tokenize("java spring hibernate"));

        var searchService = new SearchService(tokenizer, invertedIndex, scorer);

        var results = searchService.search("java spring boot", 0.6);

        assertEquals(2, results.size());
        assertTrue(results.contains(1));
        assertTrue(results.contains(2));
    }

}
