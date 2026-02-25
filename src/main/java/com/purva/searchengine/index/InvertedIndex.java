package com.purva.searchengine.index;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InvertedIndex {
    private final Map<String, Map<Integer, Posting>> index = new HashMap<>();
    private final Map<Integer, Integer> documentLengths = new HashMap<>();
    private final AtomicInteger totalDocuments = new AtomicInteger(0);

    public void index(int docId, List<String> tokens) {
        if (tokens == null || tokens.isEmpty() || docId <= 0) {
            return;
        }

        if (!documentLengths.containsKey(docId)) {
            totalDocuments.incrementAndGet();
        }
        documentLengths.put(docId, tokens.size());

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

    public Map<Integer, Posting> getPostingsMap(String token) {
        return Collections.unmodifiableMap(index.getOrDefault(token, Map.of()));
    }

    public boolean contains(String token) {
        return index.containsKey(token);
    }

    public int getDocumentLength(int docId) {
        return documentLengths.getOrDefault(docId, 1);
    }

    public int getTotalDocuments() {
        return totalDocuments.get();
    }

    public int getDocumentFrequency(String token) {
        return index.getOrDefault(token, Map.of()).size();
    }
}
