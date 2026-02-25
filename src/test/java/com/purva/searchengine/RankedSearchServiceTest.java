package com.purva.searchengine;

import com.purva.searchengine.index.InvertedIndex;
import com.purva.searchengine.service.SearchResult;
import com.purva.searchengine.service.SearchService;
import com.purva.searchengine.tokenizer.Tokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RankedSearchServiceTest {
    private SearchService searchService;

    String doc1 = "java is a programming language used word widely. java is platform independent and java is object oriented programming language. java is used for developing web applications, mobile applications, and enterprise software. java is a popular programming language for beginners and experienced developers alike.";
    String doc2 = "python programming language and is very popular for data science, machine learning, and web development. python has a simple syntax and a large community of developers who contribute to its extensive libraries and frameworks. python is used for various applications, including scientific computing, artificial intelligence, and automation.";
    String doc3 = "java, python programming are backend programming languages. java is used for enterprise applications, while python is popular for data science and machine learning. both languages have large communities and extensive libraries, making them versatile choices for developers.";

    @BeforeEach
    void setup() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();

        searchService = new SearchService(tokenizer, invertedIndex);

        var tokens = tokenizer.tokenize(doc1);
        invertedIndex.index(1, tokens);

        tokens = tokenizer.tokenize(doc2);
        invertedIndex.index(2, tokens);

        tokens = tokenizer.tokenize(doc3);
        invertedIndex.index(3, tokens);
    }

    @Test
    void shouldReturnResultsInDescendingOrderOfScore() {
        var results = searchService.rankedSearch("java programming", 3);
        List<Integer> docResult = results.stream().map(SearchResult::documentId).toList();
        List<Integer> expectedResult = new ArrayList<>(List.of(1, 3));
        assertEquals(expectedResult, docResult);

        var results2 = searchService.rankedSearch("python programming", 3);
        List<Integer> docResult2 = results2.stream().map(SearchResult::documentId).toList();
        List<Integer> expectedResult2 = new ArrayList<>(List.of(3, 2));
        assertEquals(expectedResult2, docResult2);
    }

    @Test
    void shouldReturnResultsWithSameScoreInAscendingOrderOfDocId() {
        var results = searchService.rankedSearch("programming", 3);
        List<Integer> docResult = results.stream().map(SearchResult::documentId).toList();
        List<Integer> expectedResult = new ArrayList<>(List.of(1, 3, 2));
        assertEquals(expectedResult, docResult);
    }

    @Test
    void shouldReturnAllResultsIfTopKIsGreaterThanAvailableResults() {
        var results = searchService.rankedSearch("programming", 5);
        List<Integer> docResult = results.stream().map(SearchResult::documentId).toList();
        List<Integer> expectedResult = new ArrayList<>(List.of(1, 3, 2));
        assertEquals(expectedResult, docResult);
    }

    @Test
    void shouldReturnTopKResults() {
        var results = searchService.rankedSearch("programming", 2);
        List<Integer> docResult = results.stream().map(SearchResult::documentId).toList();
        List<Integer> expectedResult = new ArrayList<>(List.of(1, 3));
        assertEquals(expectedResult, docResult);
    }

    @Test
    void shouldReturnEmptyListForNoMatchingDocuments() {
        var results = searchService.rankedSearch("ruby programming", 3);
        assertTrue(results.isEmpty());
    }

    @Test
    void shouldThrowExceptionForInvalidTopK() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> searchService.rankedSearch("java", -1),
                "Should throw when topK is negative"
        );

        assertEquals("topK must be greater than 0", exception.getMessage());

        exception = assertThrows(
                IllegalArgumentException.class,
                () -> searchService.rankedSearch("java", 0),
                "Should throw when topK is zero"
        );

        assertEquals("topK must be greater than 0", exception.getMessage());
    }

    @Test
    void shouldReturnEmptyListForEmptyOrNullQuery() {
        var results = searchService.rankedSearch("", 3);
        assertTrue(results.isEmpty());

        results = searchService.rankedSearch(null, 3);
        assertTrue(results.isEmpty());
    }

}
