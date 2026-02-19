package com.purva.searchengine.index;

import java.util.*;

public class InvertedIndex {
    private final Map<String, Map<Integer, Posting>> index = new HashMap<>();

    public void index(int docId, List<String> tokens) {
        if (tokens == null || tokens.isEmpty() || docId <= 0) {
            return;
        }

        Map<String, Integer> termFreqMap = new HashMap<>();
        for (String token : tokens) {
            termFreqMap.put(token, termFreqMap.getOrDefault(token, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : termFreqMap.entrySet()) {
            String token = entry.getKey();
            int frequency = entry.getValue();
            index.computeIfAbsent(token, k -> new HashMap<>()).put(docId, new Posting(docId, frequency));
        }
    }

    public Collection<Posting> getPostings(String token) {
        Collection<Posting> postings =  index.getOrDefault(token, Map.of()).values();
        return Collections.unmodifiableCollection(postings);
    }

    public boolean contains(String token) {
        return index.containsKey(token);
    }
}
