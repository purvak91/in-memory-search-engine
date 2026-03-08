package com.purva.searchengine.service;

import com.purva.searchengine.index.InvertedIndex;
import com.purva.searchengine.index.Posting;
import com.purva.searchengine.search.Scorer;
import com.purva.searchengine.tokenizer.Tokenizer;

import java.util.*;

public class SearchService {
    private final Tokenizer tokenizer;
    private final InvertedIndex invertedIndex;
    private final Scorer scorer;

    public SearchService(Tokenizer tokenizer, InvertedIndex invertedIndex, Scorer scorer) {
        this.tokenizer = tokenizer;
        this.invertedIndex = invertedIndex;
        this.scorer = scorer;
    }

    public List<Integer> search(String query, double threshold) {
        validateThreshold(threshold);
        List<String> tokens = tokenizer.tokenize(query);
        if (tokens.isEmpty()) {
            return List.of();
        }

        Set<Integer> candidateDocIds = getCandidateDocIds(tokens, threshold);
        return candidateDocIds.stream().sorted().toList();
    }

    public List<SearchResult> rankedSearch(String query, int topK, double threshold) {
        validateThreshold(threshold);
        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be greater than 0");
        }

        List<String> tokens = tokenizer.tokenize(query);
        if (tokens.isEmpty()) {
            return List.of();
        }

        Set<Integer> candidateDocIds = getCandidateDocIds(tokens, threshold);
        if (candidateDocIds.isEmpty()) {
            return List.of();
        }

        PriorityQueue<SearchResult> topKDocs = new PriorityQueue<>((topK + 1), Comparator.comparingDouble(SearchResult::score));

        for (Integer docId : candidateDocIds) {
            double score = 0;
            for (String token : tokens) {
                score += scorer.score(docId, token);
            }
            topKDocs.offer(new SearchResult(docId, score));
            if (topKDocs.size() > topK) {
                topKDocs.poll();
            }
        }

        List<SearchResult> results = new ArrayList<>(topKDocs);
        results.sort(Comparator.comparingDouble(SearchResult::score).reversed().thenComparingInt(SearchResult::documentId));
        return results;
    }

    private void validateThreshold(double threshold) {
        if (threshold <= 0 || threshold > 1) {
            throw new IllegalArgumentException("Threshold must be in the range (0, 1]");
        }
    }

    private Set<Integer> getCandidateDocIds(List<String> tokens, double threshold) {

        Set<Integer> candidateDocIds = new HashSet<>();
        HashMap<Integer, Integer> docIdToTokenCount = new HashMap<>();

        // Performing Threshold search: Return documents that contain more than threshold number of tokens
        for (String token : tokens) {
            var tokenPostings = invertedIndex.getPostings(token);

            for (Posting posting : tokenPostings) {
                docIdToTokenCount.merge(posting.documentId(), 1, Integer::sum);
            }
        }

        int thresholdValue = (int) Math.ceil(tokens.size() * threshold);
        for (Map.Entry<Integer, Integer> entry : docIdToTokenCount.entrySet()) {
            if (entry.getValue() >= thresholdValue) {
                candidateDocIds.add(entry.getKey());
            }
        }

        return candidateDocIds;

    }
}
