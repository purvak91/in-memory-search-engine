package com.purva.searchengine.search;

public interface Scorer {
    double score(int docId, String token);
}
