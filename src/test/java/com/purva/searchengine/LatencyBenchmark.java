package com.purva.searchengine;

import com.purva.searchengine.index.InvertedIndex;
import com.purva.searchengine.service.SearchService;
import com.purva.searchengine.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Standalone benchmark suite for measuring the latency of core engine operations.
 * * Measures:
 * 1. Document Indexing (Write Latency)
 * 2. Boolean AND Search (Read Latency)
 * 3. TF-IDF Ranked Search (Computation Latency)
 * * Note: To ensure accurate results, run this on a quiet machine with
 * minimal background processes to avoid CPU scheduling noise.
 */
public class LatencyBenchmark {

    private static void analysis(String title, List<Long> latencies, long blackHole) {
        Collections.sort(latencies);

        double avgNanos = latencies.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double avgMicros = avgNanos / 1000.0;

        long minNanos = latencies.getFirst();
        long maxNanos = latencies.getLast();
        long p99Nanos = latencies.get((int) (latencies.size() * 0.99));

        System.out.println("=== " + title + " ===");
        System.out.printf("Average Latency: %.2f µs%n", avgMicros);
        System.out.printf("Min Latency:     %.2f µs%n", minNanos / 1000.0);
        System.out.printf("P99 Latency:     %.2f µs%n", p99Nanos / 1000.0);
        System.out.printf("Max Latency:     %.2f µs%n", maxNanos / 1000.0);
        System.out.println("DCE Checksum:    " + blackHole);
        System.out.println("==============================\n");
    }

    private static void warmUpIndex(InvertedIndex invertedIndex) {
        System.out.println("Warming up Indexer...");
        for (int i = 0; i < 10000; i++) {
            invertedIndex.index(100000 + i, List.of("java", "search", "engine", "warmup"));
        }
    }

    private static void measurementIndex(InvertedIndex invertedIndex) {
        int numDocs = 10000;
        List<Long> latencies = new ArrayList<>(numDocs);
        long blackHole = 0;

        for (int i = 0; i < numDocs; i++) {
            int uniqueId = 200000 + i;
            List<String> tokens = List.of("java", "search", "engine", "test" + (i % 10));

            long startTime = System.nanoTime();
            invertedIndex.index(uniqueId, tokens);
            long endTime = System.nanoTime();

            blackHole += uniqueId;
            latencies.add(endTime - startTime);
        }
        analysis("INDEXING BENCHMARK", latencies, blackHole);
    }

    private static void warmUpSearch(SearchService searchService) {
        System.out.println("Warming up Boolean Search...");
        for (int i = 0; i < 10000; i++) {
            searchService.search("java");
        }
    }

    private static void measurementSearch(SearchService searchService) {
        int numSearches = 10000;
        List<Long> latencies = new ArrayList<>(numSearches);
        long blackHole = 0;
        String[] queries = {"java", "search", "engine", "test5"};

        for (int i = 0; i < numSearches; i++) {
            String query = queries[i % queries.length];
            long startTime = System.nanoTime();
            var results = searchService.search(query);
            long endTime = System.nanoTime();

            blackHole += results.size();
            latencies.add(endTime - startTime);
        }
        analysis("BOOLEAN SEARCH BENCHMARK", latencies, blackHole);
    }

    private static void warmUpRanked(SearchService searchService) {
        System.out.println("Warming up Ranked Search...");
        for (int i = 0; i < 10000; i++) {
            searchService.rankedSearch("java", 5);
        }
    }

    private static void measurementRanked(SearchService searchService) {
        int numSearches = 10000;
        List<Long> latencies = new ArrayList<>(numSearches);
        long blackHole = 0;
        String[] queries = {"java", "search", "engine", "test5"};

        for (int i = 0; i < numSearches; i++) {
            String query = queries[i % queries.length];
            long startTime = System.nanoTime();
            var results = searchService.rankedSearch(query, 10);
            long endTime = System.nanoTime();

            blackHole += results.size();
            latencies.add(endTime - startTime);
        }
        analysis("RANKED SEARCH BENCHMARK", latencies, blackHole);
    }

    public static void main(String[] args) {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var searchService = new SearchService(tokenizer, invertedIndex);

        for (int i = 0; i < 5000; i++) {
            invertedIndex.index(i, List.of("java", "search", "engine", "test" + (i % 10)));
        }

        warmUpIndex(invertedIndex);
        measurementIndex(invertedIndex);

        warmUpSearch(searchService);
        measurementSearch(searchService);

        warmUpRanked(searchService);
        measurementRanked(searchService);
    }
}