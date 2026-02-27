package com.purva.searchengine.index;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InvertedIndex {
    private final Map<String, Map<Integer, Posting>> index = new HashMap<>();
    private final Map<Integer, Integer> documentLengths = new HashMap<>();
    private final AtomicInteger totalDocuments = new AtomicInteger(0);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void index(int docId, List<String> tokens) {
        if (tokens == null || tokens.isEmpty() || docId <= 0) {
            return;
        }

        lock.writeLock().lock();

        try {
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
        finally {
            lock.writeLock().unlock();
        }
    }

    public Collection<Posting> getPostings(String token) {
        lock.readLock().lock();

        try {
            Collection<Posting> postings =  index.getOrDefault(token, Map.of()).values();
            return Collections.unmodifiableCollection(postings);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public Map<Integer, Posting> getPostingsMap(String token) {
        lock.readLock().lock();

        try {
            return Collections.unmodifiableMap(index.getOrDefault(token, Map.of()));
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public boolean contains(String token) {
        lock.readLock().lock();

        try {
            return index.containsKey(token);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public int getDocumentLength(int docId) {
        lock.readLock().lock();

        try {
            return documentLengths.getOrDefault(docId, 1);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public int getTotalDocuments() {
        return totalDocuments.get();
    }

    public int getDocumentFrequency(String token) {
        lock.readLock().lock();

        try {
            return index.getOrDefault(token, Map.of()).size();
        }
        finally {
            lock.readLock().unlock();
        }
    }
}
