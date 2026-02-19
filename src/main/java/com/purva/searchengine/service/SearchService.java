package com.purva.searchengine.service;

import com.purva.searchengine.index.InvertedIndex;
import com.purva.searchengine.index.Posting;
import com.purva.searchengine.tokenizer.Tokenizer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchService {
    private final Tokenizer tokenizer;
    private final InvertedIndex invertedIndex;

    public SearchService(Tokenizer tokenizer, InvertedIndex invertedIndex) {
        this.tokenizer = tokenizer;
        this.invertedIndex = invertedIndex;
    }

    public List<Integer> search(String query) {
        List<String> tokens = tokenizer.tokenize(query);
        if (tokens.isEmpty()) {
            return List.of();
        }

        Set<Integer> result = new HashSet<>();

        var tokenPostings = invertedIndex.getPostings(tokens.get(0));
        if (tokenPostings.isEmpty()) {
            return List.of();
        }

        for (Posting posting : tokenPostings) {
            result.add(posting.documentId());
        }

        // Performing AND search: Only return documents that contain all tokens
        for (int i = 1; i < tokens.size(); i++) {
            String token = tokens.get(i);
            tokenPostings = invertedIndex.getPostings(token);
            if (tokenPostings.isEmpty()) {
                return List.of();
            }

            Set<Integer> tokenDocIds = new HashSet<>();
            for (Posting posting : tokenPostings) {
                tokenDocIds.add(posting.documentId());
            }
            result.retainAll(tokenDocIds);
            if (result.isEmpty()) {
                break;
            }
        }

        return result.stream().sorted().toList();
    }
}
