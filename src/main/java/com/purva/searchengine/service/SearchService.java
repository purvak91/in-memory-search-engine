package com.purva.searchengine.service;

import com.purva.searchengine.index.InvertedIndex;
import com.purva.searchengine.index.Posting;
import com.purva.searchengine.tokenizer.Tokenizer;

import java.util.*;

public class SearchService {
    private final Tokenizer tokenizer;
    private final InvertedIndex invertedIndex;
    private final TfIdfScorer scorer;

    public SearchService(Tokenizer tokenizer, InvertedIndex invertedIndex) {
        this.tokenizer = tokenizer;
        this.invertedIndex = invertedIndex;
        this.scorer = new TfIdfScorer(invertedIndex);
    }

    public List<Integer> search(String query) {
        List<String> tokens = tokenizer.tokenize(query);
        if (tokens.isEmpty()) {
            return List.of();
        }

        Set<Integer> candidateDocIds = getCandidateDocIds(tokens);
        return candidateDocIds.stream().sorted().toList();
    }

    public List<SearchResult> rankedSearch(String query, int topK) {
        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be greater than 0");
        }

        List<String> tokens = tokenizer.tokenize(query);
        if (tokens.isEmpty()) {
            return List.of();
        }

        Set<Integer> candidateDocIds = getCandidateDocIds(tokens);
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

    private Set<Integer> getCandidateDocIds(List<String> tokens) {

        Set<Integer> candidateDocIds = new HashSet<>();

        var tokenPostings = invertedIndex.getPostings(tokens.getFirst());
        for (Posting posting : tokenPostings) {
            candidateDocIds.add(posting.documentId());
        }

        // Performing AND search: Only return documents that contain all tokens
        for (int i = 1; i < tokens.size(); i++) {
            String token = tokens.get(i);
            tokenPostings = invertedIndex.getPostings(token);
            if (tokenPostings.isEmpty()) {
                return Set.of();
            }

            Set<Integer> tokenDocIds = new HashSet<>();
            for (Posting posting : tokenPostings) {
                tokenDocIds.add(posting.documentId());
            }
            candidateDocIds.retainAll(tokenDocIds);
            if (candidateDocIds.isEmpty()) {
                break;
            }
        }

        return candidateDocIds;

    }
}
